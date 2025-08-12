package com.example.dishapp.ui.detail

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
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

    private val args: DetailFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = try {
            args.id
        } catch (e: Exception) {
            Log.e("DetailFragment", "Failed to get arg id: ${e.message}")
            -1
        }

        if (id < 0) {
            Log.e("DetailFragment", "Invalid id passed: $id")
            return
        }

        val maybeDish = Data.getById(id)
        if (maybeDish == null) {
            Log.e("DetailFragment", "Dish with id $id not found")
            return
        }
        dish = maybeDish
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailBinding.bind(view)

        if (!::dish.isInitialized) {
            binding.tvName.text = getString(R.string.label_not_found)
            binding.ivPhoto.setImageResource(R.drawable.placeholder)
            return
        }

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
