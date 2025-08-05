package com.example.dishapp.models

data class Dish(
    val id: Int,
    var name: String,
    var imageUri: String?,
    var ingredients: List<String>,
    var method: DishType
)
