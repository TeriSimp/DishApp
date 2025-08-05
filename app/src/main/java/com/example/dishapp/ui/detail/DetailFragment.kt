package com.example.dishapp.ui.detail

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.dishapp.R
import com.example.dishapp.databinding.FragmentDetailBinding
import com.example.dishapp.models.Data
import com.example.dishapp.models.Dish
import java.lang.Exception
import androidx.core.net.toUri

class DetailFragment : Fragment(R.layout.fragment_detail) {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var dish: Dish

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = requireArguments().getInt(ARG_ID)
        dish = Data.getById(id)
            ?: throw IllegalStateException("Dish with id $id not found")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailBinding.bind(view)

        dish.imageUri?.takeIf { it.isNotEmpty() }?.let { uriString ->
            val uri = uriString.toUri()
            binding.ivPhoto.loadUri(uri, requireContext())
        } ?: binding.ivPhoto.setImageResource(R.drawable.placeholder)

        binding.tvName.text = dish.name
        binding.tvType.text = getString(R.string.label_method, dish.method.displayName)
        val ingList = dish.ingredients.joinToString(", ")
        binding.tvIngredients.text =
            requireContext().getString(R.string.label_ingredients, ingList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ID = "arg_id"

        fun newInstance(id: Int) = DetailFragment().apply {
            arguments = Bundle().apply { putInt(ARG_ID, id) }
        }
    }
}

private fun android.widget.ImageView.loadUri(uri: Uri, ctx: Context) {
    try {
        ctx.contentResolver.openInputStream(uri)?.use { input ->
            val bmp = BitmapFactory.decodeStream(input)
            setImageBitmap(bmp)
        } ?: setImageResource(R.drawable.placeholder)
    } catch (_: Exception) {
        setImageResource(R.drawable.placeholder)
    }
}
