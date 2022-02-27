package com.example.basicapp.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.example.basicapp.MainActivity
import com.example.basicapp.R
import com.example.basicapp.TaskApplication

fun NotificationManager.sendNotification(taskTitle: String, applicationContext: Context) {
    val intent = Intent(applicationContext, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val pendingIntent: PendingIntent = PendingIntent
        .getActivity(applicationContext, 0, intent, 0)

    val taskImage = BitmapFactory.decodeResource(
        applicationContext.resources,
        R.drawable.task
    )

    val bigPicStyle = NotificationCompat.BigPictureStyle()
        .bigPicture(taskImage)
        .bigLargeIcon(null)


    val builder = NotificationCompat.Builder(applicationContext, TaskApplication.CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("Task Reminder")
        .setContentText(taskTitle)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setStyle(bigPicStyle)
        .setLargeIcon(taskImage)




    notify(System.currentTimeMillis().toInt(), builder.build())
}