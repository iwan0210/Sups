package com.kotlin.sups.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Binder
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.kotlin.sups.R
import com.kotlin.sups.data.local.StoryDao
import com.kotlin.sups.data.local.StoryDatabase
import com.kotlin.sups.data.model.Story
import java.net.URL

internal class StackRemoteViewsFactory(private val mContext: Context) :
    RemoteViewsService.RemoteViewsFactory {

    private val mWidgetItems = ArrayList<Story>()
    private lateinit var storyDao: StoryDao

    override fun onCreate() {
        storyDao = StoryDatabase.getDatabase(mContext).storyDao()
    }

    override fun onDataSetChanged() {
        val identityToken = Binder.clearCallingIdentity()
        mWidgetItems.clear()
        mWidgetItems.addAll(storyDao.getStories())
        Binder.restoreCallingIdentity(identityToken)
    }

    override fun onDestroy() = Unit

    override fun getCount(): Int = mWidgetItems.size

    override fun getViewAt(position: Int): RemoteViews {
        val rv = RemoteViews(mContext.packageName, R.layout.widget_item)
        val bitmap = loadImageBitmap(mWidgetItems[position].photoUrl)
        rv.setImageViewBitmap(R.id.imageView, bitmap)

        return rv
    }

    private fun loadImageBitmap(imageUrl: String): Bitmap {
        return try {
            val inputStream = URL(imageUrl).openStream()
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            BitmapFactory.decodeResource(mContext.resources, R.drawable.placeholder)
        }
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = 0

    override fun hasStableIds(): Boolean = false
}