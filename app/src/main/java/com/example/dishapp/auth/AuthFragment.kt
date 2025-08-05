package com.example.dishapp.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.example.dishapp.R
import com.example.dishapp.network.RetrofitClient
import com.example.dishapp.network.OtpRequest
import com.example.dishapp.network.OtpResponse
import kotlinx.coroutines.launch
import retrofit2.Response

class AuthFragment : Fragment(R.layout.fragment_auth) {

    private enum class Mode { PHONE, EMAIL }

    private var mode = Mode.PHONE

    private lateinit var tvAuthTitle: TextView
    private lateinit var phoneContainer: ConstraintLayout
    private lateinit var emailContainer: ConstraintLayout
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var cbAgree: CheckBox
    private lateinit var btnSignIn: MaterialButton
    private lateinit var btnAuthToggle: LinearLayout
    private lateinit var ivAuthIcon: ImageView
    private lateinit var tvAuthText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvAuthTitle = view.findViewById(R.id.tvAuthTitle)
        phoneContainer = view.findViewById(R.id.phoneInputContainer)
        emailContainer = view.findViewById(R.id.emailInputContainer)
        etPhone = view.findViewById(R.id.etPhoneNumber)
        etEmail = view.findViewById(R.id.etEmail)
        cbAgree = view.findViewById(R.id.cbAgree)
        btnSignIn = view.findViewById(R.id.btnSignIn)
        btnAuthToggle = view.findViewById(R.id.btnAuthToggle)
        ivAuthIcon = view.findViewById(R.id.ivAuthIcon)
        tvAuthText = view.findViewById(R.id.tvAuthText)

        view.isClickable = true
        view.isFocusableInTouchMode = true
        view.requestFocus()
        etPhone.clearFocus()
        etEmail.clearFocus()
        etPhone.setOnFocusChangeListener { _, hasFocus ->
            phoneContainer.isSelected = hasFocus
        }
        etEmail.setOnFocusChangeListener { _, hasFocus ->
            emailContainer.isSelected = hasFocus
        }
        view.setOnClickListener {
            etPhone.clearFocus()
            etEmail.clearFocus()
        }

        cbAgree.setOnCheckedChangeListener { _, isChecked ->
            btnSignIn.isEnabled = isChecked
            btnSignIn.alpha = if (isChecked) 1f else 0.5f
        }

        btnAuthToggle.setOnClickListener {
            mode = if (mode == Mode.PHONE) Mode.EMAIL else Mode.PHONE
            updateUI()
        }

        btnSignIn.setOnClickListener {
            if (!cbAgree.isChecked) {
                Toast.makeText(
                    requireContext(),
                    "You must agree to Terms first",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val input = if (mode == Mode.PHONE) etPhone.text.toString() else etEmail.text.toString()
            val valid = when (mode) {
                Mode.PHONE -> input.length >= 10 && input.all { it.isDigit() }
                Mode.EMAIL -> Patterns.EMAIL_ADDRESS.matcher(input).matches() &&
                        input.endsWith("@gmail.com", ignoreCase = true)
            }

            if (!valid) {
                val what = if (mode == Mode.PHONE) "phone number" else "email"
                Toast.makeText(
                    requireContext(),
                    "Invalid $what. Please check and retry.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (mode == Mode.PHONE) {
                requestOtp(input)
            } else {
                val action = AuthFragmentDirections
                    .actionAuthToVerifyCode(contact = input)
                findNavController().navigate(action)
            }
        }

        updateUI()
        btnSignIn.isEnabled = false
        btnSignIn.alpha = 0.5f
    }

    private fun updateUI() {
        if (mode == Mode.PHONE) {
            tvAuthTitle.text = getString(R.string.enter_your_phone_number_to_sign_in)
            phoneContainer.visibility = View.VISIBLE
            emailContainer.visibility = View.GONE
            ivAuthIcon.setImageResource(R.drawable.ic_email)
            tvAuthText.text = getString(R.string.email)
        } else {
            tvAuthTitle.text = getString(R.string.enter_your_email_address_to_sign_in)
            phoneContainer.visibility = View.GONE
            emailContainer.visibility = View.VISIBLE
            ivAuthIcon.setImageResource(R.drawable.ic_phone)
            tvAuthText.text = getString(R.string.phone)
        }
    }

    private fun requestOtp(phone: String) {
        lifecycleScope.launch {
            try {
                val body = OtpRequest(identifier = phone)
                val response: Response<OtpResponse> =
                    RetrofitClient.authService.getOtp(body = body)

                if (response.isSuccessful) {
                    val action = AuthFragmentDirections
                        .actionAuthToVerifyCode(contact = phone)
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(
                        requireContext(),
                        response.body()?.message ?: "Failed to get otp",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Cannot connect: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
            }
        }
    }
}