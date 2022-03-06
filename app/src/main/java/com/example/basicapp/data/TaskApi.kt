package com.example.basicapp.data

import com.example.basicapp.data.model.Task
import kotlinx.coroutines.flow.Flow
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface TaskApi {

    @GET("/tasks")
    suspend fun getTasks(): Response<List<Task>>

    @GET("/search/{query}")
    suspend fun searchTask(
        @Path("query") query: String
    ): Response<List<Task>>

    @GET("/tasks/{id}")
    suspend fun getTask(
        @Path("id") id: Int
    ): Response<Task>

    @POST("/tasks")
    suspend fun addTask(
        @Body params : Task
    ) : Response<Task>

    @HTTP(method = "DELETE", path = "/tasks/{id}", hasBody = true)
    suspend fun deleteTask(
        @Path("id") id : Int,
    ) : Response<Task>
}