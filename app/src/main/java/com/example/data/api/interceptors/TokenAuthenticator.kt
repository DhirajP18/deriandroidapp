package com.example.data.api.interceptors

import com.example.data.api.ApiJsonParser
import com.example.data.local.DataStoreManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.RequestBody.Companion.toRequestBody

class TokenAuthenticator(
    private val dataStoreManager: DataStoreManager,
    private val baseUrl: String,
) : Authenticator {
    override fun authenticate(route: okhttp3.Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null
        if (response.request.url.encodedPath.contains("Auth/Refresh", ignoreCase = true)) return null

        val session = runBlocking { dataStoreManager.sessionFlow.firstOrNull() } ?: return null
        val token = refreshToken() ?: return null

        runBlocking {
            dataStoreManager.saveSession(session.copy(accessToken = token))
        }

        return response.request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }

    private fun refreshToken(): String? {
        return try {
            val client = OkHttpClient.Builder().build()
            val request = Request.Builder()
                .url("${baseUrl.trimEnd('/')}/Auth/Refresh")
                .post("{}".toRequestBody())
                .header("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { res ->
                if (!res.isSuccessful) return null
                val body = res.body?.string() ?: return null
                val payload = ApiJsonParser.parseObject(body)
                val data = ApiJsonParser.readNestedObject(payload, "Data", "data", "Result", "result") ?: payload
                ApiJsonParser.readString(data, "AccessToken", "accessToken")
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

}
