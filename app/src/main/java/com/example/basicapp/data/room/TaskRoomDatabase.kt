package com.example.basicapp.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.basicapp.data.model.Task

@Database(entities = [Task::class], version = 7, exportSchema = false)
abstract class TaskRoomDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskRoomDatabase? = null

        fun getDatabase(context: Context): TaskRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskRoomDatabase::class.java,
                    "item_database"
                ).fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}
