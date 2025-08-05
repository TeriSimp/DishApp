package com.example.dishapp.models

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Prefs {
    private const val PREF_NAME   = "dish_prefs"
    private const val KEY_DISHES  = "key_dishes"
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveDishes(list: List<Dish>) {
        val json = gson.toJson(list)
        prefs.edit {
            putString(KEY_DISHES, json)
        }
    }

    fun loadDishes(): ArrayList<Dish> {
        val json = prefs.getString(KEY_DISHES, null) ?: return arrayListOf()
        val type = object : TypeToken<ArrayList<Dish>>() {}.type
        return gson.fromJson(json, type)
    }
}
