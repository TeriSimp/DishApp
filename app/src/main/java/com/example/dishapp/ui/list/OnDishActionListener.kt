package com.example.dishapp.ui.list

import com.example.dishapp.models.Dish

interface OnDishActionListener {
    fun onView(dish: Dish)

    fun onEdit(dish: Dish)

    fun onDelete(dish: Dish)
}