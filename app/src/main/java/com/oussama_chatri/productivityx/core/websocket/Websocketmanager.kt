package com.oussama_chatri.productivityx.core.websocket

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

sealed class WebSocketEvent {
    data object Connected : WebSocketEvent()
    data class Message(val payload: String) : WebSocketEvent()
    data object Closing : WebSocketEvent()
    data class Failure(val throwable: Throwable?) : WebSocketEvent()
}

@Singleton
class WebSocketManager @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private var webSocket: WebSocket? = null

    fun observe(url: String, bearerToken: String): Flow<WebSocketEvent> = callbackFlow {
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $bearerToken")
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                webSocket = ws
                trySend(WebSocketEvent.Connected)
            }

            override fun onMessage(ws: WebSocket, text: String) {
                trySend(WebSocketEvent.Message(text))
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                trySend(WebSocketEvent.Closing)
                ws.close(1000, null)
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                trySend(WebSocketEvent.Failure(t))
                close(t)
            }
        }

        webSocket = okHttpClient.newWebSocket(request, listener)
        awaitClose { disconnect() }
    }

    fun send(message: String): Boolean = webSocket?.send(message) ?: false

    fun disconnect() {
        webSocket?.close(1000, "Client disconnecting")
        webSocket = null
    }
}