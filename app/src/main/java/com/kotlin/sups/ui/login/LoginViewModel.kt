package com.kotlin.sups.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kotlin.sups.data.Result
import com.kotlin.sups.data.StoryRepository
import com.kotlin.sups.data.model.Login
import com.kotlin.sups.helper.Event
import kotlinx.coroutines.launch

class LoginViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    private val _result = MutableLiveData<Event<Result<Login>>>()
    val result: LiveData<Event<Result<Login>>> = _result

    fun getLoginState(): LiveData<Boolean> = storyRepository.getLoginState().asLiveData()

    fun saveLoginState(loginState: Boolean) {
        viewModelScope.launch {
            storyRepository.saveLoginState(loginState)
        }
    }

    fun saveToken(token: String) {
        viewModelScope.launch {
            storyRepository.saveToken(token)
        }
    }

    fun login(email: String, password: String) {
        storyRepository.login(email, password).observeForever {
            _result.value = Event(it)
        }
    }
}