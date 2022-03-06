package com.example.basicapp.data.model

import retrofit2.Response

sealed class ApiResponse<T>(
    val data : T? = null,
    val message: String? = null
) {
    class Success<T>(data: T, message: String) : ApiResponse<T>(data, message)
    class Error<T>( message: String) : ApiResponse<T>(null,message)
}

/*sealed class ApiResponse<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T, message: String) : ApiResponse<T>(data, message)
    class Error<T>(message: String, data: T? = null,  ) : ApiResponse<T>(data, message)
    class Loading<T> : ApiResponse<T>()
}*/
