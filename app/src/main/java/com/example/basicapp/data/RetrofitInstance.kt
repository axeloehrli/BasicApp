package com.example.basicapp.data

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type

object RetrofitInstance {
    val api: TaskApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.100.13:3000")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TaskApi::class.java)
    }
}