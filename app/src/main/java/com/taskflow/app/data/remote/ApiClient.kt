package com.taskflow.app.data.remote

import com.google.gson.GsonBuilder
import com.taskflow.app.BuildConfig
import com.taskflow.app.data.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    fun create(session: SessionManager): ApiService {
        val authInterceptor = Interceptor { chain ->
            val request = chain.request()
            val builder = request.newBuilder()
            session.token?.let { builder.header("Authorization", "Bearer $it") }
            val response = chain.proceed(builder.build())
            val path = request.url.encodedPath
            // A 401 on a protected route means the JWT expired. (Auth and password routes use 401 for wrong credentials.)
            if (response.code == 401 && !path.contains("/api/auth/") && !path.endsWith("/password")) {
                session.sessionExpired.tryEmit(Unit)
            }
            response
        }

        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }

        // Generous timeouts: the free Render tier can take ~50s to wake up.
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(70, TimeUnit.SECONDS)
            .readTimeout(70, TimeUnit.SECONDS)
            .writeTimeout(70, TimeUnit.SECONDS)
            .build()

        // serializeNulls: sync sends explicit nulls (e.g. dueDate = null) so the server clears the field.
        val gson = GsonBuilder().serializeNulls().create()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
