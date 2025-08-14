package com.example.dishapp.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.bumptech.glide.Glide
import com.example.dishapp.R
import com.example.dishapp.data.AuthRepository
import com.example.dishapp.data.TokenStore
import com.example.dishapp.databinding.FragmentAccountBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*

class AccountFragment : Fragment(R.layout.fragment_account) {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val TAG = "AccountFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentAccountBinding.bind(view)

        binding.btnLogout.setOnClickListener {
            performLogout()
        }

        val repo = AuthRepository(requireContext())

        val token = TokenStore.getToken(requireContext())
        val projectId = TokenStore.getProjectId(requireContext())
        val storedUserId = TokenStore.getUserId(requireContext())

        if (!token.isNullOrEmpty() && !projectId.isNullOrEmpty() && !storedUserId.isNullOrEmpty()) {
            showLoadingState()

            lifecycleScope.launch {
                try {
                    when (val res = repo.getUserFromStoredToken()) {
                        is AuthRepository.Result.Success -> {
                            val user = res.data
                            bindApiUser(
                                name = user.name,
                                id = user.id,
                                avatar = user.avatar,
                                aboutMe = user.aboutMe,
                                projectId = user.projectId,
                                email = user.email,
                                phone = user.phone
                            )
                        }

                        is AuthRepository.Result.Failure -> {
                            val msg = res.message
                            Log.w(TAG, "getUserFromStoredToken failed: $msg")
                            if (msg.contains("Unauthorized", ignoreCase = true)) {
                                TokenStore.clear(requireContext())
                                Toast.makeText(
                                    requireContext(),
                                    "Session expired. Please verify again.",
                                    Toast.LENGTH_LONG
                                ).show()
                                showNoUser()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to load profile: ${res.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                showFirebaseFallback()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected error while fetching user", e)
                    Toast.makeText(
                        requireContext(),
                        "Network error while loading account",
                        Toast.LENGTH_SHORT
                    ).show()
                    showFirebaseFallback()
                }
            }
        } else {
            showFirebaseFallback()
        }
    }

    private fun performLogout() {
        binding.btnLogout.isEnabled = false

        lifecycleScope.launch {
            try {
                TokenStore.clear(requireContext())

                try {
                    FirebaseAuth.getInstance().signOut()
                } catch (e: Exception) {
                    Log.w(TAG, "Firebase signOut error", e)
                }

                Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()

                val nav = findNavController()
                val opts = navOptions {
                    popUpTo(nav.graph.id) { inclusive = true }
                }
                nav.navigate(R.id.authFragment, null, opts)

            } catch (e: Exception) {
                Log.e(TAG, "Logout failed", e)
                Toast.makeText(requireContext(), "Logout failed: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            } finally {
                binding.btnLogout.isEnabled = true
            }
        }
    }

    private fun showLoadingState() {
        binding.tvName.text = getString(R.string.loading)
        binding.tvEmail.text = ""
        binding.tvOtherInfo.text = ""
        binding.imgAvatar.setImageResource(R.drawable.ic_account_placeholder)
    }

    private fun bindApiUser(
        name: String?,
        id: String?,
        avatar: String?,
        aboutMe: String?,
        projectId: String?,
        email: String?,
        phone: String?
    ) {
        if (!avatar.isNullOrEmpty()) {
            try {
                Glide.with(this)
                    .load(avatar)
                    .circleCrop()
                    .into(binding.imgAvatar)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load avatar", e)
                binding.imgAvatar.setImageResource(R.drawable.ic_account_placeholder)
            }
        } else {
            binding.imgAvatar.setImageResource(R.drawable.ic_account_placeholder)
        }

        binding.tvName.text = name ?: id ?: getString(R.string.no_user)
        binding.tvEmail.text = email ?: ""

        val sb = StringBuilder()
        id?.let { sb.append("id: $it\n") }
        projectId?.let { sb.append("project_id: $it\n") }
        phone?.let { sb.append("phone: $it\n") }
        aboutMe?.let { sb.append("about_me: $it\n") }

        binding.tvOtherInfo.text =
            if (sb.isNotEmpty()) sb.toString() else getString(R.string.null_user)

        Log.d(TAG, "API user info:\n${binding.tvOtherInfo.text}")
    }

    private fun showFirebaseFallback() {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (user == null) {
            binding.tvName.text = getString(R.string.no_user)
            binding.tvEmail.text = ""
            binding.tvOtherInfo.text = getString(R.string.null_user)
            Log.w(TAG, "FirebaseUser is null")
            return
        }

        val photoUrl = user.photoUrl
        if (photoUrl != null) {
            try {
                Glide.with(this)
                    .load(photoUrl)
                    .circleCrop()
                    .into(binding.imgAvatar)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load Firebase avatar", e)
                binding.imgAvatar.setImageResource(R.drawable.ic_account_placeholder)
            }
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
            val df = DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM,
                DateFormat.SHORT,
                Locale.getDefault()
            )
            val creation =
                if (meta.creationTimestamp > 0) df.format(Date(meta.creationTimestamp)) else "(null)"
            val lastSign =
                if (meta.lastSignInTimestamp > 0) df.format(Date(meta.lastSignInTimestamp)) else "(null)"
            sb.append("metadata.creationTime: $creation\n")
            sb.append("metadata.lastSignInTime: $lastSign\n")
        } else {
            sb.append("metadata: (null)\n")
        }

        sb.append("\nOther available getters:\n")
        sb.append("  getDisplayName() -> ${user.displayName ?: "(null)"}\n")
        sb.append("  getEmail() -> ${user.email ?: "(null)"}\n")
        sb.append("  getProviderId() -> ${user.providerId}\n")

        binding.tvOtherInfo.text = sb.toString()
        Log.d(TAG, "FirebaseUser info:\n${binding.tvOtherInfo.text}")
    }

    private fun showNoUser() {
        binding.tvName.text = getString(R.string.no_user)
        binding.tvEmail.text = ""
        binding.tvOtherInfo.text = getString(R.string.null_user)
        binding.imgAvatar.setImageResource(R.drawable.ic_account_placeholder)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
