package com.oussama_chatri.productivityx.features.ai.data.repository

import com.oussama_chatri.productivityx.core.enums.MessageRole
import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.features.ai.data.local.dao.ConversationDao
import com.oussama_chatri.productivityx.features.ai.data.local.dao.MessageDao
import com.oussama_chatri.productivityx.features.ai.data.local.entity.MessageEntity
import com.oussama_chatri.productivityx.features.ai.data.local.mapper.toDomain
import com.oussama_chatri.productivityx.features.ai.data.remote.api.AiApiService
import com.oussama_chatri.productivityx.features.ai.data.remote.dto.request.AiContextRequest
import com.oussama_chatri.productivityx.features.ai.data.remote.dto.request.CreateConversationRequest
import com.oussama_chatri.productivityx.features.ai.data.remote.dto.request.SendMessageRequest
import com.oussama_chatri.productivityx.features.ai.data.remote.dto.response.toEntity
import com.oussama_chatri.productivityx.features.ai.domain.model.AiContext
import com.oussama_chatri.productivityx.features.ai.domain.model.Conversation
import com.oussama_chatri.productivityx.features.ai.domain.model.Message
import com.oussama_chatri.productivityx.features.ai.domain.repository.AiRepository
import com.oussama_chatri.productivityx.features.ai.domain.repository.StreamChunk
import com.oussama_chatri.productivityx.features.events.data.local.EventDao
import com.oussama_chatri.productivityx.features.notes.data.local.NoteDao
import com.oussama_chatri.productivityx.features.pomodoro.data.local.dao.PomodoroSessionDao
import com.oussama_chatri.productivityx.features.tasks.data.local.dao.TaskDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class AiRepositoryImpl @Inject constructor(
    private val conversationDao     : ConversationDao,
    private val messageDao          : MessageDao,
    private val noteDao             : NoteDao,
    private val taskDao             : TaskDao,
    private val eventDao            : EventDao,
    private val pomodoroDao         : PomodoroSessionDao,
    private val apiService          : AiApiService,
    private val okHttpClient        : OkHttpClient,
    @Named("base_url") baseUrl      : String,
    private val json                : Json,
    private val preferencesDataStore: PreferencesDataStore,
) : AiRepository {

    private val baseUrl = baseUrl.trimEnd('/')

    // Conversations

    override fun observeConversations(): Flow<List<Conversation>> =
        conversationDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getConversation(id: UUID): Conversation? =
        conversationDao.findById(id)?.toDomain()

    override suspend fun createConversation(title: String?): Conversation =
        withContext(Dispatchers.IO) {
            val remote       = apiService.createConversation(CreateConversationRequest(title))
            val remoteEntity = remote.toEntity()
            conversationDao.insert(remoteEntity)
            remoteEntity.toDomain()
        }

    override suspend fun deleteConversation(id: UUID): Unit = withContext(Dispatchers.IO) {
        conversationDao.deleteById(id)
        runCatching { apiService.deleteConversation(id.toString()) }
    }

    override suspend fun archiveConversation(id: UUID): Unit = withContext(Dispatchers.IO) {
        conversationDao.archive(id, Instant.now())
    }

    // Messages

    override fun observeMessages(conversationId: UUID): Flow<List<Message>> =
        messageDao.observeByConversation(conversationId).map { list -> list.map { it.toDomain() } }

    override suspend fun getMessages(conversationId: UUID): List<Message> =
        withContext(Dispatchers.IO) {
            messageDao.findByConversation(conversationId).map { it.toDomain() }
        }

    // SSE Streaming

    override fun sendMessage(
        conversationId: UUID,
        content: String,
        context: AiContext,
    ): Flow<StreamChunk> = callbackFlow {

        val userEntity = MessageEntity(
            id              = UUID.randomUUID(),
            conversationId  = conversationId,
            role            = MessageRole.USER,
            content         = content,
            actionBlockJson = null,
            tokenCount      = null,
            createdAt       = Instant.now(),
        )
        messageDao.insert(userEntity)
        conversationDao.refreshSummary(conversationId, content, Instant.now())

        val requestBody = json.encodeToString(
            SendMessageRequest(
                content = content,
                context = AiContextRequest(
                    tasksDueToday          = context.tasksDueToday,
                    tasksOverdue           = context.tasksOverdue,
                    totalActiveTasks       = context.totalActiveTasks,
                    upcomingEventsThisWeek = context.upcomingEventsThisWeek,
                    lastEditedNoteTitle    = context.lastEditedNoteTitle,
                    currentPomodoroTask    = context.currentPomodoroTask,
                    todayFocusMinutes      = context.todayFocusMinutes,
                ),
            )
        ).toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$baseUrl/api/v1/ai/conversations/$conversationId/messages")
            .post(requestBody)
            .addHeader("Accept", "text/event-stream")
            .build()

        val tokenBuffer     = StringBuilder()
        var actionBlockJson : String? = null

        // SSE needs no read timeout — connection stays open until stream ends.
        // newBuilder() inherits all interceptors (auth, logging) from the parent client.
        val sseClient = okHttpClient.newBuilder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val listener = object : EventSourceListener() {
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String,
            ) {
                when (type) {
                    "token" -> {
                        tokenBuffer.append(data)
                        trySend(StreamChunk.Token(data))
                    }
                    "action" -> {
                        actionBlockJson = data
                    }
                    "done" -> {
                        val now            = Instant.now()
                        val fullContent    = tokenBuffer.toString()
                        val assistantMsgId = try {
                            JSONObject(data).getString("message_id").let(UUID::fromString)
                        } catch (_: Exception) {
                            UUID.randomUUID()
                        }

                        val assistantEntity = MessageEntity(
                            id              = assistantMsgId,
                            conversationId  = conversationId,
                            role            = MessageRole.ASSISTANT,
                            content         = fullContent,
                            actionBlockJson = actionBlockJson,
                            tokenCount      = null,
                            createdAt       = now,
                        )
                        kotlinx.coroutines.runBlocking {
                            messageDao.insert(assistantEntity)
                            conversationDao.refreshSummary(conversationId, fullContent, now)
                        }
                        trySend(StreamChunk.Done(assistantEntity.toDomain()))
                        close()
                    }
                }
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: okhttp3.Response?,
            ) {
                val error = t ?: Exception("SSE failed — HTTP ${response?.code}")
                trySend(StreamChunk.Error(error))
                close()
            }
        }

        val eventSource = EventSources.createFactory(sseClient)
            .newEventSource(request, listener)

        awaitClose { eventSource.cancel() }
    }

    // Context Builder

    override suspend fun buildContext(): AiContext = withContext(Dispatchers.IO) {
        val userId = preferencesDataStore.cachedUserId.first() ?: return@withContext emptyContext()

        val today      = LocalDate.now()
        val zone       = ZoneId.systemDefault()
        val todayStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val todayEnd   = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val weekEnd    = today.plusDays(7).atStartOfDay(zone).toInstant().toEpochMilli()

        val tasksDueToday    = runCatching { taskDao.countDueToday(userId, today) }.getOrElse { 0 }
        val tasksOverdue     = runCatching { taskDao.countOverdue(userId, today) }.getOrElse { 0 }
        val totalActiveTasks = runCatching { taskDao.countActive(userId) }.getOrElse { 0 }
        val upcomingEvents   = runCatching {
            eventDao.countUpcomingThisWeek(userId, todayStart, weekEnd)
        }.getOrElse { 0 }
        val lastNote         = runCatching { noteDao.lastEditedTitle(userId) }.getOrElse { null }
        val todayFocus       = runCatching {
            pomodoroDao.totalFocusMinutesToday(userId, todayStart, todayEnd)
        }.getOrElse { 0 }

        AiContext(
            tasksDueToday          = tasksDueToday,
            tasksOverdue           = tasksOverdue,
            totalActiveTasks       = totalActiveTasks,
            upcomingEventsThisWeek = upcomingEvents,
            lastEditedNoteTitle    = lastNote,
            currentPomodoroTask    = null,
            todayFocusMinutes      = todayFocus,
        )
    }

    private fun emptyContext() = AiContext(
        tasksDueToday          = 0,
        tasksOverdue           = 0,
        totalActiveTasks       = 0,
        upcomingEventsThisWeek = 0,
        lastEditedNoteTitle    = null,
        currentPomodoroTask    = null,
        todayFocusMinutes      = 0,
    )
}