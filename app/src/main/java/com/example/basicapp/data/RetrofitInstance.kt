package com.example.basicapp.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    val api: TaskApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.100.11:3000")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(TaskApi::class.java)
    }
}