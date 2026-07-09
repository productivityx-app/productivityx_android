package com.oussama_chatri.productivityx.features.notes.data.repository

import com.oussama_chatri.productivityx.core.storage.PreferencesDataStore
import com.oussama_chatri.productivityx.core.util.Resource
import com.oussama_chatri.productivityx.features.notes.data.local.FolderDao
import com.oussama_chatri.productivityx.features.notes.data.local.FolderEntity
import com.oussama_chatri.productivityx.features.notes.data.local.NoteDao
import com.oussama_chatri.productivityx.features.notes.data.mapper.toDomain
import com.oussama_chatri.productivityx.features.notes.domain.model.NoteFolder
import com.oussama_chatri.productivityx.features.notes.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepositoryImpl @Inject constructor(
    private val folderDao: FolderDao,
    private val noteDao: NoteDao,
    private val preferencesDataStore: PreferencesDataStore
) : FolderRepository {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun observeFolders(): Flow<List<NoteFolder>> =
        preferencesDataStore.cachedUserId.map { it ?: "" }.flatMapLatest { userId ->
            folderDao.observeFolders(userId).map { entities ->
                entities.map { folder ->
                    val count = folderDao.countNotesInFolder(folder.id)
                    folder.toDomain(count.toInt())
                }
            }
        }

    override suspend fun createFolder(name: String, color: String?, parentFolderId: String?): Resource<NoteFolder> {
        val userId = cachedUserId()
        val entity = FolderEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = name.trim(),
            parentFolderId = parentFolderId,
            color = color ?: "#6366F1",
            createdAt = Instant.now().toEpochMilli()
        )
        folderDao.upsert(entity)
        return Resource.Success(entity.toDomain(0))
    }

    override suspend fun updateFolder(folderId: String, name: String, color: String?): Resource<NoteFolder> {
        val existing = folderDao.getFolderById(folderId) ?: return Resource.Error("Folder not found")
        val updated = existing.copy(name = name.trim(), color = color ?: existing.color)
        folderDao.upsert(updated)
        val count = folderDao.countNotesInFolder(folderId)
        return Resource.Success(updated.toDomain(count.toInt()))
    }

    override suspend fun deleteFolder(folderId: String): Resource<Unit> {
        folderDao.deleteById(folderId)
        return Resource.Success(Unit)
    }

    private suspend fun cachedUserId(): String =
        preferencesDataStore.cachedUserId.first() ?: ""
}
