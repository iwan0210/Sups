package com.kotlin.sups.helper

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kotlin.sups.data.StoryRepository
import com.kotlin.sups.di.Injection
import com.kotlin.sups.ui.addStory.AddStoryViewModel
import com.kotlin.sups.ui.detail.DetailViewModel
import com.kotlin.sups.ui.home.MainViewModel
import com.kotlin.sups.ui.login.LoginViewModel
import com.kotlin.sups.ui.maps.MapsViewModel
import com.kotlin.sups.ui.register.RegisterViewModel

class ViewModelFactory private constructor(private val storyRepository: StoryRepository) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java))
            return MainViewModel(storyRepository) as T
        if (modelClass.isAssignableFrom(LoginViewModel::class.java))
            return LoginViewModel(storyRepository) as T
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java))
            return RegisterViewModel(storyRepository) as T
        if (modelClass.isAssignableFrom(DetailViewModel::class.java))
            return DetailViewModel(storyRepository) as T
        if (modelClass.isAssignableFrom(AddStoryViewModel::class.java))
            return AddStoryViewModel(storyRepository) as T
        if (modelClass.isAssignableFrom(MapsViewModel::class.java))
            return MapsViewModel(storyRepository) as T
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }

    companion object {
        fun getInstance(context: Context): ViewModelFactory =
            synchronized(this) {
                ViewModelFactory(Injection.provideRepository(context))
            }
    }
}