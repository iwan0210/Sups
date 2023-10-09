package com.kotlin.sups.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kotlin.sups.data.Result
import com.kotlin.sups.data.StoryRepository
import com.kotlin.sups.data.model.Error
import com.kotlin.sups.helper.Event

class RegisterViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    private val _result = MutableLiveData<Event<Result<Error>>>()
    val result: LiveData<Event<Result<Error>>> = _result

    fun register(
        name: String, email: String, password: String
    ) {
        storyRepository.register(name, email, password).observeForever {
            _result.value = Event(it)
        }
    }
}