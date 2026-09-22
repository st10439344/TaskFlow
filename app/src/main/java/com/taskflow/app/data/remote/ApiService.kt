package com.taskflow.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
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
}
