package com.example.dishapp.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.dishapp.R
import com.example.dishapp.databinding.FragmentAccountBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import java.text.DateFormat
import java.util.*

class AccountFragment : Fragment(R.layout.fragment_account) {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val TAG = "AccountFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentAccountBinding.bind(view)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            binding.tvName.text = getString(R.string.no_user)
            binding.tvEmail.text = ""
            binding.tvOtherInfo.text = getString(R.string.null_user)
            Log.w(TAG, "FirebaseUser is null")
            return
        }

        val photoUrl = user.photoUrl
        if (photoUrl != null) {
            Glide.with(this)
                .load(photoUrl)
                .circleCrop()
                .into(binding.imgAvatar)
        } else {
            binding.imgAvatar.setImageResource(R.drawable.ic_account_placeholder)
        }

        binding.tvName.text = user.displayName ?: "(no displayName)"
        binding.tvEmail.text = user.email ?: "(no email)"

        val sb = StringBuilder()
        sb.append("uid: ${user.uid}\n")
        sb.append("providerId (user.providerId): ${user.providerId}\n")
        sb.append("phoneNumber: ${user.phoneNumber ?: "(null)"}\n")
        sb.append("isEmailVerified: ${user.isEmailVerified}\n")
        sb.append("photoUrl: ${user.photoUrl ?: "(null)"}\n")

        val meta = user.metadata
        if (meta != null) {
            val df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
            val creation = meta.creationTimestamp.let { df.format(Date(it)) } ?: "(null)"
            val lastSign = meta.lastSignInTimestamp.let { df.format(Date(it)) } ?: "(null)"
            sb.append("metadata.creationTime: $creation\n")
            sb.append("metadata.lastSignInTime: $lastSign\n")
        } else {
            sb.append("metadata: (null)\n")
        }

        sb.append("\nOther available getters:\n")
        sb.append("  getDisplayName() -> ${user.displayName ?: "(null)"}\n")
        sb.append("  getEmail() -> ${user.email ?: "(null)"}\n")
        sb.append("  getProviderId() -> ${user.providerId}\n")

        val otherInfo = sb.toString()

        binding.tvOtherInfo.text = otherInfo
        Log.d(TAG, "FirebaseUser info:\n$otherInfo")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
