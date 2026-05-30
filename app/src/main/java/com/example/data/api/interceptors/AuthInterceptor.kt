package com.example.data.api.interceptors

import com.example.data.local.DataStoreManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val dataStoreManager: DataStoreManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip adding token for login and refresh endpoints explicitly
        val path = originalRequest.url.encodedPath
        if (path.contains("Auth/Login") || path.contains("Auth/Refresh")) {
            return chain.proceed(originalRequest)
        }

        val session = runBlocking { dataStoreManager.sessionFlow.firstOrNull() }
        val token = session?.accessToken

        val request = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}
