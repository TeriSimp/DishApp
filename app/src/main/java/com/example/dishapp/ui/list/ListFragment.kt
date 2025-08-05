package com.example.dishapp.ui.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dishapp.R
import com.example.dishapp.databinding.FragmentDishesListBinding
import com.example.dishapp.models.Data
import com.example.dishapp.models.Dish
import com.example.dishapp.ui.detail.DetailFragment
import com.example.dishapp.ui.edit.EditFragment

class ListFragment : Fragment(R.layout.fragment_dishes_list),
    OnDishActionListener {

    private var _binding: FragmentDishesListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DishAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDishesListBinding.bind(view)

        setupRecycler()
        setupFab()
    }

    private fun setupRecycler() {
        adapter = DishAdapter(this)

        binding.rvList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvList.adapter = adapter

        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onChanged() = updateEmptyView()
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = updateEmptyView()
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = updateEmptyView()
        })

        submitAndUpdate()
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, EditFragment.newInstance(null))
                .addToBackStack(null)
                .commit()
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
        binding.rvList.visibility  = if (empty) View.GONE  else View.VISIBLE
    }

    override fun onView(dish: Dish) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, DetailFragment.newInstance(dish.id))
            .addToBackStack(null)
            .commit()
    }

    override fun onEdit(dish: Dish) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, EditFragment.newInstance(dish.id))
            .addToBackStack(null)
            .commit()
    }

    override fun onDelete(dish: Dish) {
        if (Data.remove(dish.id)) {
            submitAndUpdate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
