package com.taskflow.app.data.remote

import com.google.gson.Gson
import retrofit2.Response
import java.io.IOException

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(
        val message: String,
        val code: Int = 0,
        val fieldErrors: Map<String, String> = emptyMap()
    ) : ApiResult<Nothing>() {
        val isNetwork: Boolean get() = code == NETWORK_ERROR
    }

    companion object {
        const val NETWORK_ERROR = -1
    }
}

/** Wraps a Retrofit call so network failures and server errors never crash the app. */
suspend fun <T> safeCall(block: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = block()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) ApiResult.Success(body) else ApiResult.Error("Empty response from server", response.code())
        } else {
            val raw = try { response.errorBody()?.string() } catch (e: Exception) { null }
            val parsed = try { Gson().fromJson(raw, ErrorResponse::class.java) } catch (e: Exception) { null }
            ApiResult.Error(
                parsed?.message ?: "Server error (${response.code()})",
                response.code(),
                parsed?.errors ?: emptyMap()
            )
        }
    } catch (e: IOException) {
        ApiResult.Error("No connection to the server", ApiResult.NETWORK_ERROR)
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Unexpected error", 0)
    }
}
