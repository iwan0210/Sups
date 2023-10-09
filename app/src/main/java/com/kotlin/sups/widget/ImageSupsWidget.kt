package com.kotlin.sups.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.net.toUri
import com.kotlin.sups.R

class ImageSupsWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }


    override fun onEnabled(context: Context) = Unit

    override fun onDisabled(context: Context) = Unit
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val intent = Intent(context, StackWidgetService::class.java)
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    intent.data = intent.toUri(Intent.URI_INTENT_SCHEME).toUri()

    val views = RemoteViews(context.packageName, R.layout.image_sups_widget)
    views.setRemoteAdapter(R.id.stack_view, intent)
    views.setEmptyView(R.id.stack_view, R.id.empty_view)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}