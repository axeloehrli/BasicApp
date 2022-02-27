package com.example.basicapp.data.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.basicapp.data.model.Task
import com.example.basicapp.data.room.TaskRoomDatabase
import com.example.basicapp.util.sendNotification
import kotlinx.coroutines.flow.first

class TaskReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {

        val taskTitle = inputData.getString(nameKey)

        val notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager


        notificationManager.sendNotification(
            taskTitle ?: "",
            applicationContext
        )

        suspend fun getItem() : Task {
            return TaskRoomDatabase.getDatabase(applicationContext).taskDao().getItemByNotificationTag(taskTitle ?: "").first()
        }


        TaskRoomDatabase.getDatabase(applicationContext).taskDao().update(
            Task(
                getItem().id,
                getItem().notificationTag,
                "Late",
                getItem().title,
                getItem().description,
                getItem().time,
                getItem().latitude,
                getItem().longitude
            )
        )



        //currentTask.value!!.status = "Late"

        //TaskRoomDatabase.getDatabase(applicationContext).taskDao().update(currentTask.value!!)

        return Result.success()
    }

    companion object {
        const val nameKey = "NAME"
    }
}