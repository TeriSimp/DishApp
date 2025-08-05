package com.example.dishapp.models

import android.content.Context

object Data : DishRepository {
    private var list = arrayListOf<Dish>()

    fun load(context: Context) {
        Prefs.init(context)
        list = Prefs.loadDishes()
    }

    fun getNextId(): Int = (list.maxOfOrNull { it.id } ?: 0) + 1

    override fun getAll(): List<Dish> = list.toList()

    override fun getById(id: Int): Dish? = list.find { it.id == id }

    override fun add(dish: Dish) {
        list.add(dish)
        Prefs.saveDishes(list)
    }

    override fun update(dish: Dish): Boolean {
        val idx = list.indexOfFirst { it.id == dish.id }
        return if (idx != -1) {
            list[idx] = dish
            Prefs.saveDishes(list)
            true
        } else false
    }

    override fun remove(id: Int): Boolean {
        val idx = list.indexOfFirst { it.id == id }
        return if (idx != -1) {
            list.removeAt(idx)
            Prefs.saveDishes(list)
            true
        } else false
    }
}
