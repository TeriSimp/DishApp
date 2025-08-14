package com.example.dishapp.ui.auth

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hbb20.CountryCodePicker
//import com.bumptech.glide.Glide
import com.example.dishapp.R
import com.example.dishapp.network.OtpRequest
import com.example.dishapp.network.OtpResponse
import com.example.dishapp.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
//import com.google.firebase.auth.FirebaseAuth
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
    private lateinit var btnAuthGoogle: LinearLayout
    private lateinit var ccp: CountryCodePicker
    private lateinit var btnCustomPicker: ImageButton

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        val response = res.idpResponse
        if (res.resultCode == Activity.RESULT_OK) {
            Toast.makeText(
                requireContext(),
                "Login success!",
                Toast.LENGTH_SHORT
            ).show()
            Log.d("AuthFragment", "Login success: ${response?.providerType}")
            findNavController().navigate(R.id.action_auth_to_dish_list)
        } else {
            val code = response?.error?.errorCode
            Log.w("Error", "UI sign-in failed code=$code")
            Toast.makeText(
                requireContext(),
                "Authentication failed: ${response?.error?.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

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
        btnAuthGoogle = view.findViewById(R.id.btnAuthGoogle)
        ccp = view.findViewById(R.id.ccp)
        btnCustomPicker = view.findViewById(R.id.btnCustomPicker)

        btnAuthGoogle.setOnClickListener {
            val providers = listOf(
                AuthUI.IdpConfig.GoogleBuilder().build()
            )
            val intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
            signInLauncher.launch(intent)
        }

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

            if (mode == Mode.PHONE) {
                val raw = etPhone.text.toString().trim()
                val e164 = getValidatedE164Number(raw)
                if (e164 == null) {
                    Toast.makeText(
                        requireContext(),
                        "Invalid phone number. Please check and retry.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                requestOtp(e164)
            } else {
                val input = etEmail.text.toString().trim()
                val validEmail = Patterns.EMAIL_ADDRESS.matcher(input).matches() &&
                        input.endsWith("@gmail.com", ignoreCase = true)
                if (!validEmail) {
                    Toast.makeText(
                        requireContext(),
                        "Invalid email. Please check and retry.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                val action = AuthFragmentDirections
                    .actionAuthToVerifyCode(contact = input)
                findNavController().navigate(action)
            }
        }

        btnCustomPicker.setOnClickListener {
            CountryListBottomSheet { dialCode, iso2 ->
                ccp.setCountryForNameCode(iso2)
                Log.d("AuthFragment", "Custom picker selected iso2=$iso2, dialCode=$dialCode")
            }.show(childFragmentManager, "countryList")
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
            btnCustomPicker.visibility = View.VISIBLE
            ivAuthIcon.setImageResource(R.drawable.ic_email)
            tvAuthText.text = getString(R.string.email)
        } else {
            tvAuthTitle.text = getString(R.string.enter_your_email_address_to_sign_in)
            phoneContainer.visibility = View.GONE
            emailContainer.visibility = View.VISIBLE
            btnCustomPicker.visibility = View.GONE
            ivAuthIcon.setImageResource(R.drawable.ic_phone)
            tvAuthText.text = getString(R.string.phone)
        }
    }

    private fun getValidatedE164Number(raw: String): String? {
        if (raw.isEmpty()) return null
        val phoneUtil = PhoneNumberUtil.getInstance()
        val iso2 = try {
            ccp.selectedCountryNameCode ?: ""
        } catch (e: Exception) {
            Log.e("AuthFragment", "Error getting country ISO code", e)
            ""
        }

        return try {
            val numberProto = phoneUtil.parse(raw, iso2)
            if (!phoneUtil.isValidNumber(numberProto)) {
                null
            } else {
                phoneUtil.format(numberProto, PhoneNumberFormat.E164)
            }
        } catch (e: NumberParseException) {
            Log.w("AuthFragment", "NumberParseException: ${e.message}")
            null
        } catch (e: Exception) {
            Log.w("AuthFragment", "Unexpected parse error: ${e.message}")
            null
        }
    }

    private fun requestOtp(phone: String) {
        lifecycleScope.launch {
            try {
                val body = OtpRequest(identifier = phone)
                val response: Response<OtpResponse> =
                    RetrofitClient.authService(requireContext()).getOtp(body = body)

                if (response.isSuccessful) {
                    val action = AuthFragmentDirections
                        .actionAuthToVerifyCode(phone)
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
            }
        }
    }
}