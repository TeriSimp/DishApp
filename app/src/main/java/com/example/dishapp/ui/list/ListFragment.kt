package com.example.dishapp.ui.list

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dishapp.R
import com.example.dishapp.databinding.FragmentDishesListBinding
import com.example.dishapp.models.Data
import com.example.dishapp.models.Dish
import com.example.dishapp.ui.common.ConfirmActionBottomSheet

class ListFragment : Fragment(R.layout.fragment_dishes_list),
    DishAdapter.OnDishActionListener {

    private var _binding: FragmentDishesListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DishAdapter
    private var pendingDishToDelete: Dish? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDishesListBinding.bind(view)

        parentFragmentManager.setFragmentResultListener(
            ConfirmActionBottomSheet.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val confirmed =
                bundle.getBoolean(ConfirmActionBottomSheet.BUNDLE_KEY_CONFIRM, false)
            val action = bundle.getString(ConfirmActionBottomSheet.EXTRA_ACTION)

            if (!confirmed) {
                pendingDishToDelete = null
                return@setFragmentResultListener
            }

            if (action == "delete") {
                pendingDishToDelete?.let { dish ->
                    if (Data.remove(dish.id)) {
                        submitAndUpdate()
                    }
                }
                pendingDishToDelete = null
            } else {
                pendingDishToDelete = null
            }
        }

        setupRecycler()
        setupFab()
    }

    private fun setupRecycler() {
        adapter = DishAdapter(this)

        binding.rvList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvList.adapter = adapter

        adapter.registerAdapterDataObserver(object : androidx.recyclerview.widget.RecyclerView.AdapterDataObserver() {
            override fun onChanged() = updateEmptyView()
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = updateEmptyView()
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = updateEmptyView()
        })

        submitAndUpdate()
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val action = ListFragmentDirections.actionListToEdit(-1)
            findNavController().navigate(action)
        }
    }

    override fun onResume() {
        super.onResume()
        submitAndUpdate()
    }

    private fun submitAndUpdate() {
        val list = Data.getAll()
        adapter.submitList(list) {
            updateEmptyView()
        }
    }

    private fun updateEmptyView() {
        val empty = adapter.itemCount == 0
        binding.tvEmpty.visibility = if (empty) View.VISIBLE else View.GONE
        binding.rvList.visibility = if (empty) View.GONE else View.VISIBLE
    }

    override fun onView(dish: Dish) {
        val action = ListFragmentDirections.actionListToDetail(dish.id)
        findNavController().navigate(action)
    }

    override fun onEdit(dish: Dish) {
        val action = ListFragmentDirections.actionListToEdit(dish.id)
        findNavController().navigate(action)
    }

    override fun onDelete(dish: Dish) {
        pendingDishToDelete = dish

        val title = getString(R.string.delete)
        val message = getString(R.string.delete_confirm_message)

        val cancelText = getString(R.string.cancel_delete)
        val confirmText = getString(R.string.confirm_delete)

        val confirmColor = ContextCompat.getColor(requireContext(), R.color.btn_red_color)
        val cancelColor = ContextCompat.getColor(requireContext(), R.color.btn_dark_color)

        val bs = ConfirmActionBottomSheet.newInstance(
            title = title,
            message = message,
            action = "delete",
            confirmText = confirmText,
            cancelText = cancelText,
            confirmColorInt = confirmColor,
            cancelColorInt = cancelColor,
            showChevron = true
        )
        bs.show(parentFragmentManager, "confirm_delete")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
