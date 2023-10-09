package com.kotlin.sups.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthPreferences private constructor(private val dataStore: DataStore<Preferences>) {

    fun getLoginState(): Flow<Boolean> {
        return dataStore.data.map { pref ->
            pref[AUTH_KEY] ?: false
        }
    }

    fun getToken(): Flow<String> {
        return dataStore.data.map { pref ->
            pref[TOKEN_KEY] ?: "none"
        }
    }

    suspend fun saveLoginState(loginState: Boolean) {
        dataStore.edit { pref ->
            pref[AUTH_KEY] = loginState
        }
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { pref ->
            pref[TOKEN_KEY] = token
        }
    }

    suspend fun deleteToken() {
        dataStore.edit { pref ->
            pref.remove(TOKEN_KEY)
        }
    }

    companion object {
        private val AUTH_KEY = booleanPreferencesKey("isLoggedIn")
        private val TOKEN_KEY = stringPreferencesKey("token")

        @Volatile
        private var INSTANCE: AuthPreferences? = null

        fun getInstance(dataStore: DataStore<Preferences>): AuthPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = AuthPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}