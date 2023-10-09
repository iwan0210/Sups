package com.kotlin.sups.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kotlin.sups.data.StoryRepository
import com.kotlin.sups.data.model.Story
import kotlinx.coroutines.launch

class MainViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    val story: LiveData<PagingData<Story>> by lazy { storyRepository.getStories().cachedIn(viewModelScope) }


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