package com.example.dishapp.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dishapp.R
import com.example.dishapp.databinding.ItemDishBinding
import com.example.dishapp.models.Dish

class DishAdapter(
    private val listener: OnDishActionListener
) : ListAdapter<Dish, DishAdapter.VH>(DiffCallback) {

    inner class VH(private val binding: ItemDishBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(dish: Dish) = with(binding) {
            dish.imageUri?.let { uriString ->
                ivDish.setImageURI(uriString.toUri())
            } ?: ivDish.setImageResource(R.drawable.placeholder)

            tvName.text = dish.name
            tvType.text = dish.method.displayName

            root.setOnClickListener  { listener.onView(dish)  }
            btnEdit.setOnClickListener   { listener.onEdit(dish)  }
            btnDelete.setOnClickListener { listener.onDelete(dish) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemDishBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    private object DiffCallback : DiffUtil.ItemCallback<Dish>() {
        override fun areItemsTheSame(oldItem: Dish, newItem: Dish) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Dish, newItem: Dish) =
            oldItem == newItem
    }
}
