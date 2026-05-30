package com.example.data.api

import android.content.Context
import com.example.data.api.interceptors.AuthInterceptor
import com.example.data.api.interceptors.TokenAuthenticator
import com.example.data.local.DataStoreManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {
    private const val BASE_URL = "https://drmstms.runasp.net/restapi/v1.0/"

    private var apiService: ApiService? = null
    private var dataStoreManager: DataStoreManager? = null

    fun getDataStore(context: Context): DataStoreManager {
        return dataStoreManager ?: synchronized(this) {
            val instance = DataStoreManager(context.applicationContext)
            dataStoreManager = instance
            instance
        }
    }

    fun getApiService(context: Context): ApiService {
        return apiService ?: synchronized(this) {
            val ds = getDataStore(context)
            
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(ds))
                .authenticator(TokenAuthenticator(ds, BASE_URL))
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            val service = retrofit.create(ApiService::class.java)
            apiService = service
            service
        }
    }
    
    // Resolve relative URL assets
    fun resolveAssetUrl(relativeUrl: String?): String {
        if (relativeUrl.isNullOrEmpty()) return ""
        if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://")) {
            return relativeUrl
        }
        val cleanRelative = relativeUrl.removePrefix("/")
        return "https://drmstms.runasp.net/$cleanRelative"
    }
}
