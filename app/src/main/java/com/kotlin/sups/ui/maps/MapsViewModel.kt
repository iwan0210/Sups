package com.kotlin.sups.ui.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.sups.data.Result
import com.kotlin.sups.data.StoryRepository
import com.kotlin.sups.data.model.Story
import kotlinx.coroutines.launch

class MapsViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    private val _result = MutableLiveData<Result<List<Story>>>()
    val result: LiveData<Result<List<Story>>> = _result

    fun getStoriesWithLocation() {
        viewModelScope.apply {
            storyRepository.getStoriesWithLocation().observeForever {
                _result.value = it
            }
        }
    }

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