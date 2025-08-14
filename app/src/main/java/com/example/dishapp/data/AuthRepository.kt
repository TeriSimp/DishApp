package com.example.dishapp.data

import android.content.Context
import android.util.Log
import com.example.dishapp.network.RetrofitClient
import com.example.dishapp.network.UserResponse
import com.example.dishapp.network.VerifyRequest
import com.example.dishapp.network.VerifyResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class AuthRepository(private val context: Context) {

    companion object {
        private const val TAG = "AuthRepository"
        private const val API_KEY = "kUCqqbfEQxkZge7HHDFcIxfoHzqSZUam"
    }

    private val api by lazy { RetrofitClient.authService(context) }

    suspend fun verifyOtpAndFetchUser(verifyReq: VerifyRequest): Result<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val verifyResp: Response<VerifyResponse> = api.verifyOtp(
                apiKey = API_KEY,
                userId = verifyReq.identifier,
                body = verifyReq
            )

            if (!verifyResp.isSuccessful) {
                val err = verifyResp.errorBody()?.string()
                return@withContext Result.Failure("Verify OTP failed: ${verifyResp.code()} ${err ?: ""}")
            }

            val verifyBody = verifyResp.body() ?: return@withContext Result.Failure("Empty verify response")

            val token = verifyBody.token
            val projectId = verifyBody.project_id
            val userId = verifyBody.user_id

            if (token.isEmpty() || projectId.isNullOrEmpty() || userId.isNullOrEmpty()) {
                return@withContext Result.Failure("Missing token/projectId/userId in verify response")
            }

            TokenStore.save(context, token, projectId, userId)

            val userResp = api.getUserInfo(
                authorization = "Bearer $token",
                userId = userId,
                projectId = projectId,
                apiKey = API_KEY,
            )

            if (userResp.isSuccessful) {
                val user = userResp.body()
                return@withContext if (user != null) {
                    try {
                        TokenStore.saveUser(context, user)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to save user cache", e)
                    }
                    Result.Success(user)
                } else {
                    Result.Failure("Empty user body")
                }
            } else {
                if (userResp.code() == 401) {
                    TokenStore.clear(context)
                    return@withContext Result.Failure("Unauthorized")
                }
                val err = userResp.errorBody()?.string()
                return@withContext Result.Failure("Get user failed: ${userResp.code()} ${err ?: ""}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "verifyOtpAndFetchUser error", e)
            return@withContext Result.Failure("Network error: ${e.message}")
        }
    }

    suspend fun getUserFromStoredToken(): Result<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val token = TokenStore.getToken(context) ?: return@withContext Result.Failure("No token")
            val projectId = TokenStore.getProjectId(context) ?: return@withContext Result.Failure("No projectId")
            val userId = TokenStore.getUserId(context) ?: return@withContext Result.Failure("No userId")

            val resp = api.getUserInfo(
                authorization = "Bearer $token",
                userId = userId,
                projectId = projectId,
                apiKey = API_KEY,
            )

            return@withContext if (resp.isSuccessful) {
                resp.body()?.let { user ->
                    try {
                        TokenStore.saveUser(context, user)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to save user cache", e)
                    }
                    Result.Success(user)
                } ?: Result.Failure("Empty response body")
            } else {
                if (resp.code() == 401) {
                    TokenStore.clear(context)
                    Result.Failure("Unauthorized")
                } else {
                    val err = resp.errorBody()?.string()
                    Result.Failure("Get user failed: ${resp.code()} ${err ?: ""}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getUserFromStoredToken error", e)
            Result.Failure("Network error: ${e.message}")
        }
    }

    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Failure(val message: String) : Result<Nothing>()
    }
}
