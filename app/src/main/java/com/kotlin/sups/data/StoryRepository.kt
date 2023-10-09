package com.kotlin.sups.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.google.gson.Gson
import com.kotlin.sups.data.datastore.AuthPreferences
import com.kotlin.sups.data.local.StoryDatabase
import com.kotlin.sups.data.model.Error
import com.kotlin.sups.data.model.Login
import com.kotlin.sups.data.model.Story
import com.kotlin.sups.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class StoryRepository(
    private val authPreferences: AuthPreferences,
    private val apiService: ApiService,
    private val storyDatabase: StoryDatabase
) {
    fun getLoginState(): Flow<Boolean> = authPreferences.getLoginState()

    suspend fun saveLoginState(loginState: Boolean) = authPreferences.saveLoginState(loginState)

    suspend fun saveToken(token: String) = authPreferences.saveToken(token)

    suspend fun deleteToken() = authPreferences.deleteToken()

    @OptIn(ExperimentalPagingApi::class)
    fun getStories(): LiveData<PagingData<Story>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData

    }

    fun getStoriesWithLocation(): LiveData<Result<List<Story>>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.getStoriesWithLocation()
            emit(Result.Success(response.listStory))
        } catch (e: Exception) {
            if (e is HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, Error::class.java)
                emit(Result.Error(errorBody.message))
            } else {
                emit(Result.Error(e.message.toString()))
            }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String
    ): LiveData<Result<Error>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.register(name, email, password)
            emit(Result.Success(response))
        } catch (e: Exception) {
            if (e is HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, Error::class.java)
                emit(Result.Error(errorBody.message))
            } else {
                emit(Result.Error(e.message.toString()))
            }
        }
    }

    fun login(
        email: String,
        password: String
    ): LiveData<Result<Login>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.login(email, password)
            emit(Result.Success(response.loginResult))
        } catch (e: Exception) {
            if (e is HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, Error::class.java)
                emit(Result.Error(errorBody.message))
            } else {
                emit(Result.Error(e.message.toString()))
            }
        }
    }

    fun postStory(
        file: MultipartBody.Part,
        description: RequestBody,
        lat: Float?,
        lon: Float?
    ): LiveData<Result<Error>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.addStory(file, description, lat, lon)
            emit(Result.Success(response))
        } catch (e: Exception) {
            if (e is HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, Error::class.java)
                emit(Result.Error(errorBody.message))
            } else {
                emit(Result.Error(e.message.toString()))
            }
        }
    }
}