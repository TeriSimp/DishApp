package com.example.dishapp.ui.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dishapp.R
import com.example.dishapp.data.AuthRepository
import com.example.dishapp.data.TokenStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment(R.layout.fragment_splash) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            delay(250) // 250ms

            val token = TokenStore.getToken(requireContext())
            val projectId = TokenStore.getProjectId(requireContext())
            val userId = TokenStore.getUserId(requireContext())

            val cachedUser = TokenStore.getUser(requireContext())

            if (!token.isNullOrEmpty() && !projectId.isNullOrEmpty() && !userId.isNullOrEmpty()) {
                if (cachedUser != null) {
                    navigateToList(clearBackStack = true)
                    return@launch
                }

                val repo = AuthRepository(requireContext())
                when (val r = repo.getUserFromStoredToken()) {
                    is AuthRepository.Result.Success -> {
                        navigateToList(clearBackStack = true)
                    }
                    is AuthRepository.Result.Failure -> {
                        navigateToAuth()
                    }
                }
            } else {
                navigateToAuth()
            }
        }
    }

    private fun navigateToList(clearBackStack: Boolean) {
        val nav = findNavController()
        val actionId = R.id.listFragment
        if (clearBackStack) {
            nav.navigate(actionId, null, androidx.navigation.navOptions {
                popUpTo(R.id.authFragment) { inclusive = true }
            })
        } else {
            nav.navigate(actionId)
        }
    }

    private fun navigateToAuth() {
        findNavController().navigate(R.id.authFragment)
    }
}
