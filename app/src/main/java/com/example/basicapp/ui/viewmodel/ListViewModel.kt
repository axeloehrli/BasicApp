package com.example.basicapp.ui.viewmodel

import androidx.lifecycle.*
import com.example.basicapp.data.RetrofitInstance
import com.example.basicapp.data.model.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

sealed class ApiResponse<T> {
    data class Success<T>(val data: T?) : ApiResponse<T>()
    data class Error<T>(val error: Throwable) : ApiResponse<T>()
}

class ListViewModel : ViewModel() {

    private var _searchTasksResponse = MutableSharedFlow<ApiResponse<List<Task>>>()
    val searchTasksResponse = _searchTasksResponse

    private var _getTasksByTimeResponse = MutableSharedFlow<ApiResponse<List<Task>>>(replay = 1)
    val getTasksByTimeResponse = _getTasksByTimeResponse

    private var _getTasksByPriorityResponse = MutableSharedFlow<ApiResponse<List<Task>>>(replay = 1)
    val getTasksByPriorityResponse = _getTasksByPriorityResponse

    @ExperimentalCoroutinesApi
    fun sortTasksByPriority() {
        viewModelScope.launch {
            _getTasksByTimeResponse.resetReplayCache()
            try {
                _getTasksByPriorityResponse.emit(
                    ApiResponse.Success(
                        RetrofitInstance.api.sortTasksByPriority().body()
                    )
                )
            } catch (t: Throwable) {
                _getTasksByPriorityResponse.emit(
                    ApiResponse.Error(t)
                )
            }
        }
    }

    fun getTasksByTime() {
        viewModelScope.launch {
            try {
                _getTasksByTimeResponse.emit(
                    ApiResponse.Success(
                        RetrofitInstance.api.sortTasksByTime().body()
                    )
                )
            } catch (t: Throwable) {
                _getTasksByTimeResponse.emit(
                    ApiResponse.Error(t)
                )
            }
        }
    }

    fun searchTasks(query: String) {
        viewModelScope.launch {
            try {
                _searchTasksResponse.emit(
                    ApiResponse.Success(
                        RetrofitInstance.api.searchTasks(query).body()
                    )
                )
            } catch (t: Throwable) {
                _searchTasksResponse.emit(
                    ApiResponse.Error(t)
                )
            }
        }
    }

}


