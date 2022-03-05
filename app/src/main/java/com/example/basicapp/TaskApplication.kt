package com.example.basicapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build

class TaskApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createChannel(applicationContext, CHANNEL_ID, "Task Reminder")
    }

    companion object {
        const val CHANNEL_ID = "water_reminder_id"
    }
}

private fun createChannel(context: Context,channelId: String, channelName: String) {
    // TODO: Step 1.6 START create a channel
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
            channelId,
            channelName,
            // TODO: Step 2.4 change importance
            NotificationManager.IMPORTANCE_HIGH
        )// TODO: Step 2.6 disable badges for this channel
            .apply {
                setShowBadge(false)
            }

        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationChannel.description = "Reminds you of Tasks"

        val notificationManager = context.getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(notificationChannel)

    }
    // TODO: Step 1.6 END create a channel
}