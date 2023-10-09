package com.kotlin.sups.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.kotlin.sups.data.StoryRepository
import com.kotlin.sups.data.datastore.AuthPreferences
import com.kotlin.sups.data.local.StoryDatabase
import com.kotlin.sups.data.remote.ApiConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

val Context.datastore: DataStore<Preferences> by preferencesDataStore("auth")

object Injection {
    fun provideRepository(context: Context): StoryRepository {
        val pref = AuthPreferences.getInstance(context.datastore)
        val token = runBlocking { pref.getToken().first() }
        val apiService = ApiConfig.getApiService(token)
        val database = StoryDatabase.getDatabase(context)
        return StoryRepository(pref, apiService, database)
    }

}