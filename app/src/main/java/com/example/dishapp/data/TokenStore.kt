package com.example.dishapp.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.dishapp.network.UserResponse
import com.google.gson.Gson

@Suppress("DEPRECATION")
object TokenStore {
    private const val PREF_FILE = "auth_prefs_v1"
    private const val KEY_TOKEN = "key_token"
    private const val KEY_PROJECT = "key_project"
    private const val KEY_USER_ID = "key_user_id"
    private const val KEY_USER_JSON = "key_user_json"

    private fun prefs(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREF_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Exception) {
            context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        }
    }

    fun save(context: Context, token: String, projectId: String, userId: String) {
        prefs(context).edit {
            putString(KEY_TOKEN, token)
            putString(KEY_PROJECT, projectId)
            putString(KEY_USER_ID, userId)
        }
    }

    fun getToken(context: Context): String? = prefs(context).getString(KEY_TOKEN, null)
    fun getProjectId(context: Context): String? = prefs(context).getString(KEY_PROJECT, null)
    fun getUserId(context: Context): String? = prefs(context).getString(KEY_USER_ID, null)
    fun clear(context: Context) = prefs(context).edit { clear() }

    fun saveUser(context: Context, user: UserResponse) {
        try {
            val gson = Gson()
            val json = gson.toJson(user)
            prefs(context).edit { putString(KEY_USER_JSON, json) }
        } catch (_: Exception) { /* ignore */
        }
    }

    fun getUser(context: Context): UserResponse? {
        val json = prefs(context).getString(KEY_USER_JSON, null) ?: return null
        return try {
            val gson = Gson()
            gson.fromJson(json, UserResponse::class.java)
        } catch (_: Exception) {
            null
        }
    }
}
