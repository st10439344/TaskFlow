package com.taskflow.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @POST("api/auth/google")
    suspend fun google(@Body body: GoogleRequest): Response<AuthResponse>

    @GET("api/users/me")
    suspend fun me(): Response<UserEnvelope>

    @PUT("api/users/me")
    suspend fun updateMe(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<UserEnvelope>

    @HTTP(method = "DELETE", path = "api/users/me")
    suspend fun deleteAccount(): Response<MessageResponse>

    @PUT("api/users/me/password")
    suspend fun changePassword(@Body body: PasswordRequest): Response<MessageResponse>

    @GET("api/lists")
    suspend fun getLists(): Response<ListsResponse>

    @POST("api/lists")
    suspend fun createList(@Body body: ListRequest): Response<ListEnvelope>

    @PUT("api/lists/{id}")
    suspend fun renameList(@Path("id") id: String, @Body body: ListRequest): Response<ListEnvelope>

    @DELETE("api/lists/{id}")
    suspend fun deleteList(@Path("id") id: String): Response<MessageResponse>

    @GET("api/tasks")
    suspend fun getTasks(): Response<TasksResponse>

    @POST("api/sync")
    suspend fun sync(@Body body: SyncRequest): Response<SyncResponse>

    // Push notifications (Firebase Cloud Messaging)
    @POST("api/devices/token")
    suspend fun registerDevice(@Body body: DeviceTokenRequest): Response<MessageResponse>

    @HTTP(method = "DELETE", path = "api/devices/token", hasBody = true)
    suspend fun unregisterDevice(@Body body: DeviceTokenRequest): Response<MessageResponse>

    @POST("api/devices/test")
    suspend fun sendTestPush(): Response<TestPushResponse>
}
