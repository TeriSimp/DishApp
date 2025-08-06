package com.example.dishapp.ui.auth

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.os.CountDownTimer
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dishapp.R
import com.example.dishapp.network.OtpRequest
import com.example.dishapp.network.RetrofitClient
import com.example.dishapp.network.VerifyRequest
import com.example.dishapp.network.VerifyResponse
import kotlinx.coroutines.launch
import retrofit2.Response

class VerifyCodeFragment : Fragment(R.layout.fragment_verify_code) {

    private lateinit var tvVerifySubtitle: TextView
    private lateinit var btnResend: Button
    private lateinit var tvTimer: TextView
    private lateinit var tvToken: TextView
    private var timer: CountDownTimer? = null
    private lateinit var etDigits: List<EditText>

    private val authService = RetrofitClient.authService
    private val apiKey = "kUCqqbfEQxkZge7HHDFcIxfoHzqSZUam"
    private val userId = "0x5c404115fab8ea5ab9fc1eff1c5575968330e384"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvVerifySubtitle = view.findViewById(R.id.tvVerifySubtitle)
        btnResend = view.findViewById(R.id.btnResend)
        tvTimer = view.findViewById(R.id.tvTimer)
        tvToken = view.findViewById(R.id.tvToken)
        etDigits = listOf(
            view.findViewById(R.id.etDigit1),
            view.findViewById(R.id.etDigit2),
            view.findViewById(R.id.etDigit3),
            view.findViewById(R.id.etDigit4),
            view.findViewById(R.id.etDigit5),
            view.findViewById(R.id.etDigit6)
        )

        val contact = VerifyCodeFragmentArgs.fromBundle(requireArguments()).contact
        tvVerifySubtitle.text =
            getString(R.string.verification_message, contact)

        view.findViewById<ImageButton>(R.id.btnBack)
            .setOnClickListener {
                (requireContext()
                    .getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(view.windowToken, 0)
                findNavController().popBackStack()
            }

        etDigits.forEachIndexed { index, edit ->
            edit.doAfterTextChanged { text ->
                if (text?.length == 1) {
                    if (index < etDigits.lastIndex) {
                        etDigits[index + 1].requestFocus()
                    } else {
                        edit.clearFocus()
                        (requireContext()
                            .getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                            .hideSoftInputFromWindow(edit.windowToken, 0)
                        tryAutoVerify(contact)
                    }
                }
            }
            edit.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL
                    && event.action == KeyEvent.ACTION_DOWN
                    && edit.text.isEmpty()
                    && index > 0
                ) {
                    etDigits[index - 1].requestFocus()
                    etDigits[index - 1].setSelection(etDigits[index - 1].text.length)
                }
                false
            }
        }

        btnResend.isEnabled = false
        btnResend.alpha = 0.5f
        startCountdown()
        btnResend.setOnClickListener {
            btnResend.isEnabled = false
            btnResend.alpha = 0.5f
            startCountdown()
            requestOtp(contact)
        }
    }

    private fun tryAutoVerify(contact: String) {
        val otp = etDigits.joinToString(separator = "") { it.text.toString().trim() }
        if (otp.length < 6) return

        etDigits.forEach { it.isEnabled = false }

        lifecycleScope.launch {
            try {
                val request = VerifyRequest(
                    identifier = contact,
                    apikey = apiKey,
                    otp = otp,
                    method = "Sms",
                )
                val resp: Response<VerifyResponse> =
                    authService.verifyOtp(apiKey, userId, request)
                if (resp.isSuccessful) {
                    val token = resp.body()?.token.orEmpty()
                    tvToken.visibility = View.VISIBLE
                    tvToken.text = getString(R.string.token_display, token)
                    Toast.makeText(
                        requireContext(),
                        "Verification successful",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    clearOtpInputs()
                    tvToken.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Incorrect OTP, please try again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (_: Exception) {
                clearOtpInputs()
                tvToken.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Network error, please try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun clearOtpInputs() {
        etDigits.forEach {
            it.setText("")
            it.isEnabled = true
        }
        etDigits[0].requestFocus()
    }

    private fun startCountdown() {
        timer?.cancel()
        timer = object : CountDownTimer(60_000, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1_000).toInt()
                tvTimer.text = getString(R.string.otp_timer, seconds)
            }
            override fun onFinish() {
                tvTimer.text = getString(R.string.otp_timer, 0)
                btnResend.isEnabled = true
                btnResend.alpha = 1f
            }
        }.start()
    }

    private fun requestOtp(contact: String) {
        lifecycleScope.launch {
            try {
                val body = OtpRequest(identifier = contact)
                val response = authService.getOtp(
                    apiKey = apiKey,
                    body = body
                )
                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "OTP has been sent",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to send OTP",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (_: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Network error, please try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
    }
}
