package com.kotlin.sups.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kotlin.sups.data.StoryRepository
import kotlinx.coroutines.launch

class DetailViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    fun getLoginState(): LiveData<Boolean> = storyRepository.getLoginState().asLiveData()

    fun saveLoginState(loginState: Boolean) {
        viewModelScope.launch {
            storyRepository.saveLoginState(loginState)
        }
    }

    fun deleteToken() {
        viewModelScope.launch {
            storyRepository.deleteToken()
        }
    }
}