package com.example.dishapp.models

interface DishRepository {
    fun getAll(): List<Dish>

    fun getById(id: Int): Dish?

    fun add(dish: Dish)

    fun update(dish: Dish): Boolean

    fun remove(id: Int): Boolean
}
