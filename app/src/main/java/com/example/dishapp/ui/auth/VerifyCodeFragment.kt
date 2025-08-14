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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dishapp.R
import com.example.dishapp.data.AuthRepository
import com.example.dishapp.network.OtpRequest
import com.example.dishapp.network.RetrofitClient
import com.example.dishapp.network.VerifyRequest
import kotlinx.coroutines.launch

class VerifyCodeFragment : Fragment(R.layout.fragment_verify_code) {

    private lateinit var tvVerifySubtitle: TextView
    private lateinit var btnResend: Button
    private lateinit var tvTimer: TextView
    private var timer: CountDownTimer? = null
    private lateinit var etDigits: List<EditText>

    private val apiKey = "kUCqqbfEQxkZge7HHDFcIxfoHzqSZUam"

    private lateinit var repo: AuthRepository
    private lateinit var viewModel: AuthViewModel

    private var isTimerFinished = false
    private var isLoadingState = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvVerifySubtitle = view.findViewById(R.id.tvVerifySubtitle)
        btnResend = view.findViewById(R.id.btnResend)
        tvTimer = view.findViewById(R.id.tvTimer)
        etDigits = listOf(
            view.findViewById(R.id.etDigit1),
            view.findViewById(R.id.etDigit2),
            view.findViewById(R.id.etDigit3),
            view.findViewById(R.id.etDigit4),
            view.findViewById(R.id.etDigit5),
            view.findViewById(R.id.etDigit6)
        )

        val contact = VerifyCodeFragmentArgs.fromBundle(requireArguments()).contact
        tvVerifySubtitle.text = getString(R.string.verification_message, contact)

        view.findViewById<ImageButton>(R.id.btnBack)
            .setOnClickListener {
                (requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(view.windowToken, 0)
                findNavController().popBackStack()
            }

        repo = AuthRepository(requireContext())
        val factory = AuthViewModelFactory(repo, requireContext())
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            isLoadingState = isLoading
            etDigits.forEach { it.isEnabled = !isLoading }
            updateResendUI()
        }

        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                findNavController().navigate(R.id.action_verifyCodeFragment_to_listFragment)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { err ->
            err?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                clearOtpInputs()
            }
        }

        etDigits.forEachIndexed { index, edit ->
            edit.doAfterTextChanged { text ->
                if (text?.length == 1) {
                    if (index < etDigits.lastIndex) {
                        etDigits[index + 1].requestFocus()
                    } else {
                        edit.clearFocus()
                        (requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
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

        isTimerFinished = false
        btnResend.isEnabled = false
        btnResend.alpha = 0.5f
        tvTimer.text = getString(R.string.otp_timer, 60)
        startCountdown()

        btnResend.setOnClickListener {
            isTimerFinished = false
            updateResendUI()
            tvTimer.text = getString(R.string.otp_timer, 60)
            startCountdown()
            requestOtp(contact)
        }
    }

    private fun tryAutoVerify(contact: String) {
        val otp = etDigits.joinToString(separator = "") { it.text.toString().trim() }
        if (otp.length < 6) return

        etDigits.forEach { it.isEnabled = false }

        val verifyReq = VerifyRequest(
            identifier = contact,
            apikey = apiKey,
            otp = otp,
            method = "Sms"
        )

        viewModel.verifyAndFetchUser(verifyReq)
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
        isTimerFinished = false
        updateResendUI()

        timer = object : CountDownTimer(60_000, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1_000).toInt()
                tvTimer.text = getString(R.string.otp_timer, seconds)
            }

            override fun onFinish() {
                tvTimer.text = getString(R.string.otp_timer, 0)
                isTimerFinished = true
                updateResendUI()
            }
        }.start()
    }

    private fun updateResendUI() {
        val enable = !isLoadingState && isTimerFinished
        btnResend.isEnabled = enable
        btnResend.alpha = if (enable) 1f else 0.5f
    }

    private fun requestOtp(contact: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val body = OtpRequest(identifier = contact)
                val response = RetrofitClient.authService(requireContext()).getOtp(
                    apiKey = apiKey,
                    body = body
                )
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "OTP has been sent", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to send OTP", Toast.LENGTH_SHORT)
                        .show()
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
