package com.kotlin.sups.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kotlin.sups.data.model.Story

@Dao
interface StoryDao {

    @Query("SELECT * FROM story")
    fun getStories(): List<Story>

    @Query("SELECT * FROM story")
    fun getAllStory(): PagingSource<Int, Story>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStories(story: List<Story>)

    @Query("DELETE FROM story")
    suspend fun deleteStories()
}