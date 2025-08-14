package com.example.dishapp.ui.auth

import android.content.Context
import androidx.lifecycle.*
import com.example.dishapp.data.AuthRepository
import com.example.dishapp.network.VerifyRequest
import com.example.dishapp.network.UserResponse
import kotlinx.coroutines.launch

class AuthViewModel(private val repo: AuthRepository) :
    ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _user = MutableLiveData<UserResponse?>()
    val user: LiveData<UserResponse?> = _user

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun verifyAndFetchUser(verifyReq: VerifyRequest) {
        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            when (val res = repo.verifyOtpAndFetchUser(verifyReq)) {
                is AuthRepository.Result.Success -> {
                    _user.value = res.data
                }

                is AuthRepository.Result.Failure -> {
                    _error.value = res.message
                }
            }
            _loading.value = false
        }
    }
}

class AuthViewModelFactory(private val repo: AuthRepository, private val context: Context) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
