package com.example.dishapp.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.SwipeLayout
import com.example.dishapp.R
import com.example.dishapp.databinding.ItemDishBinding
import com.example.dishapp.models.Dish

class DishAdapter(
    private val listener: OnDishActionListener
) : ListAdapter<Dish, DishAdapter.VH>(DiffCallback) {

    private var openSwipeLayout: SwipeLayout? = null

    inner class VH(private val binding: ItemDishBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.layoutSwipe.showMode = SwipeLayout.ShowMode.PullOut
            binding.layoutSwipe.addDrag(SwipeLayout.DragEdge.Right, binding.backgroundLayout)

            binding.layoutSwipe.addSwipeListener(object : SwipeLayout.SwipeListener {
                override fun onStartOpen(layout: SwipeLayout) {
                    if (openSwipeLayout != null && openSwipeLayout !== layout) {
                        openSwipeLayout?.close()
                    }
                    openSwipeLayout = layout
                }

                override fun onOpen(layout: SwipeLayout) {}

                override fun onStartClose(layout: SwipeLayout) {}

                override fun onClose(layout: SwipeLayout) {
                    if (openSwipeLayout === layout) {
                        openSwipeLayout = null
                    }
                }

                override fun onUpdate(layout: SwipeLayout, leftOffset: Int, topOffset: Int) {}

                override fun onHandRelease(layout: SwipeLayout, xvel: Float, yvel: Float) {}
            })
        }

        fun bind(dish: Dish) = with(binding) {
            dish.imageUri?.let { uriString ->
                ivDish.setImageURI(uriString.toUri())
            } ?: ivDish.setImageResource(R.drawable.placeholder)

            tvName.text = dish.name
            tvType.text = dish.method.displayName

            foregroundCard.setOnClickListener { listener.onView(dish) }

            btnEditSwipe.setOnClickListener {
                listener.onEdit(dish)
                binding.layoutSwipe.close()
            }

            btnDeleteSwipe.setOnClickListener {
                listener.onDelete(dish)
                binding.layoutSwipe.close()
            }
        }

        fun closeSwipe() {
            binding.layoutSwipe.close()
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

    override fun onViewRecycled(holder: VH) {
        holder.closeSwipe()
        super.onViewRecycled(holder)
    }

    private object DiffCallback : DiffUtil.ItemCallback<Dish>() {
        override fun areItemsTheSame(oldItem: Dish, newItem: Dish) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Dish, newItem: Dish) =
            oldItem == newItem
    }

    interface OnDishActionListener {
        fun onView(dish: Dish)
        fun onEdit(dish: Dish)
        fun onDelete(dish: Dish)
    }
}
