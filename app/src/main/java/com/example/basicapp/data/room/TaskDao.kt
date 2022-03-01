package com.example.basicapp.data.room

import androidx.room.*
import com.example.basicapp.data.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * from task")
    fun getItems(): Flow<List<Task>>

    @Query("SELECT * from task ORDER BY time")
    fun getItemsByTime() : Flow<List<Task>>

    @Query("SELECT * from task WHERE id = :id")
    fun getItem(id: Int): Flow<Task>

    @Query("SELECT * from task WHERE notification_tag = :notificationTag")
    fun getItemByNotificationTag(notificationTag : String) : Flow<Task>

}
