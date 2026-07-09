package com.oussama_chatri.productivityx.core.network

import com.oussama_chatri.productivityx.core.storage.TokenStorage
import io.mockk.every
import io.mockk.mockk
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TokenRefreshInterceptorTest {

    private lateinit var tokenStorage: TokenStorage
    private lateinit var refreshCookieInterceptor: RefreshCookieInterceptor
    private lateinit var interceptor: TokenRefreshInterceptor

    @Before
    fun setup() {
        tokenStorage = mockk(relaxed = true)
        refreshCookieInterceptor = mockk(relaxed = true)
        interceptor = TokenRefreshInterceptor(tokenStorage, refreshCookieInterceptor)
    }

    @Test
    fun `authenticate returns null if Authorization header is missing`() {
        // Arrange
        val request = Request.Builder()
            .url("https://api.productivityx.com/data")
            .build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        // Act
        val result = interceptor.authenticate(mockk<Route>(), response)

        // Assert
        assertNull("Should return null if the original request didn't have Authorization header", result)
    }

    @Test
    fun `authenticate returns null if response count is 2 or more`() {
        // Arrange
        val request = Request.Builder()
            .url("https://api.productivityx.com/data")
            .header(ApiConstants.HEADER_AUTHORIZATION, "Bearer old-token")
            .build()

        val priorResponse = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .priorResponse(priorResponse)
            .build()

        // Act
        val result = interceptor.authenticate(mockk<Route>(), response)

        // Assert
        assertNull("Should return null to avoid infinite refresh loops", result)
    }
}
