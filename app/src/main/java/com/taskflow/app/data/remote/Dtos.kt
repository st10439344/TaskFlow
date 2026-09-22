package com.taskflow.app.data.remote

data class RegisterRequest(val fullName: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class GoogleRequest(val idToken: String)
data class PasswordRequest(val currentPassword: String, val newPassword: String)

data class UserDto(
    val id: String,
    val fullName: String,
    val email: String,
    val authProvider: String?,
    val preferredLanguage: String?,
    val notificationsEnabled: Boolean?,
    val theme: String?,
    val streakCount: Int?,
    val weeklyCompletedCount: Int?
)

data class AuthResponse(val user: UserDto, val token: String)
data class UserEnvelope(val user: UserDto)
data class MessageResponse(val message: String?)
data class ErrorResponse(val message: String?, val errors: Map<String, String>?)

data class ListDto(val id: String, val name: String, val colorTag: String?)
data class ListRequest(val name: String, val colorTag: String? = null)
data class ListsResponse(val lists: List<ListDto>)
data class ListEnvelope(val list: ListDto)

data class SubtaskDto(val id: String? = null, val title: String, val isComplete: Boolean)

data class TaskDto(
    val id: String,
    val listId: String,
    val clientId: String?,
    val title: String,
    val description: String?,
    val dueDate: String?,
    val dueTime: String?,
    val priority: String?,
    val repeatRule: String?,
    val isComplete: Boolean,
    val subtasks: List<SubtaskDto>?
)

data class StatsDto(val streakCount: Int, val weeklyCompletedCount: Int)
data class TasksResponse(val tasks: List<TaskDto>, val stats: StatsDto?)

data class SyncChange(
    val clientId: String,
    val listId: String,
    val title: String,
    val description: String,
    val dueDate: String?,
    val dueTime: String?,
    val priority: String,
    val repeatRule: String,
    val isComplete: Boolean,
    val subtasks: List<SubtaskDto>,
    val updatedAt: String,
    val deleted: Boolean
)

data class SyncRequest(val tasks: List<SyncChange>)

data class SyncResultDto(
    val clientId: String?,
    val status: String,
    val serverId: String?,
    val task: TaskDto?,
    val message: String?
)

data class SyncResponse(val results: List<SyncResultDto>, val stats: StatsDto?)
