package com.kotlin.sups.ui.addStory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.kotlin.sups.data.Result
import com.kotlin.sups.data.StoryRepository
import com.kotlin.sups.data.model.Error
import com.kotlin.sups.helper.Event
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddStoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    private val _result = MutableLiveData<Event<Result<Error>>>()
    val result: LiveData<Event<Result<Error>>> = _result

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

    fun postStory(
        file: MultipartBody.Part,
        description: RequestBody,
        lat: Float?,
        lon: Float?
    ) {
        storyRepository.postStory(file, description, lat, lon).observeForever {
            _result.value = Event(it)
        }
    }
}