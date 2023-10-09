package com.kotlin.sups.utils

import com.kotlin.sups.data.model.Story

object DataDummy {
    fun generateDummyStoriesResponse(): List<Story> {
        val items: MutableList<Story> = arrayListOf()
        for (i in 0..100) {
            val story = Story(
                i.toString(),
                "name + $i",
                "description $i",
                "photourl $i",
                "createAt $i"
            )
            items.add(story)
        }
        return items
    }
}