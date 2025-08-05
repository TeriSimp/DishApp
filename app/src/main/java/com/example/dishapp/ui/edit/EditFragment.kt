package com.example.dishapp.ui.edit

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.fragment.app.Fragment
import com.example.dishapp.R
import com.example.dishapp.databinding.FragmentEditBinding
import com.example.dishapp.models.Data
import com.example.dishapp.models.Dish
import com.example.dishapp.models.DishType
import androidx.core.net.toUri
import java.lang.Exception

class EditFragment : Fragment(R.layout.fragment_edit) {
    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUriString: String? = null
    private var dish: Dish? = null
    private var nextId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val idArg = arguments?.getInt(ARG_ID)
        dish = idArg?.let { Data.getById(it) }
        nextId = dish?.id ?: Data.getNextId()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEditBinding.bind(view)

        val pickImageLauncher = registerForActivityResult(OpenDocument()) { uri: Uri? ->
            uri?.let { pickedUri ->
                requireContext().contentResolver.takePersistableUriPermission(
                    pickedUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                selectedImageUriString = pickedUri.toString()
                Log.e(">>>>>", "selectimg=$selectedImageUriString")

                binding.ivPhoto.loadUri(pickedUri, requireContext())
                binding.tvPickOverlay.visibility = View.GONE
            }
        }

        dish?.imageUri?.let { uriString ->
            if (uriString.isNotEmpty()) {
                val uri = uriString.toUri()
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                binding.ivPhoto.loadUri(uri, requireContext())
                binding.tvPickOverlay.visibility = View.GONE
                selectedImageUriString = uriString
            }
        }

        val typeNames = DishType.entries.map { it.displayName }
        binding.spinnerType.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            typeNames
        )

        dish?.let { d ->
            binding.etName.setText(d.name)
            binding.etIngredients.setText(d.ingredients.joinToString(","))
            DishType.entries.indexOf(d.method).takeIf { it >= 0 }
                ?.also { binding.spinnerType.setSelection(it) }
        }

        binding.ivPhoto.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val ingredients = binding.etIngredients.text
                .toString()
                .split(",")
                .map(String::trim)
                .filter(String::isNotEmpty)
            val type = DishType.entries[binding.spinnerType.selectedItemPosition]

            if (dish == null) {
                val newDish = Dish(
                    id = nextId,
                    name = name,
                    imageUri = selectedImageUriString,
                    ingredients = ingredients,
                    method = type
                )
                Data.add(newDish)
            } else {
                dish!!.apply {
                    this.name = name
                    this.ingredients = ingredients
                    this.method = type
                    this.imageUri = selectedImageUriString
                }.also { updated ->
                    Data.update(updated)
                }
            }
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ID = "arg_id"
        fun newInstance(id: Int?) = EditFragment().apply {
            arguments = Bundle().apply {
                id?.let { putInt(ARG_ID, it) }
            }
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
