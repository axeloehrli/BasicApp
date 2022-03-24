package com.example.basicapp.ui.viewmodel

import android.app.Application
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.basicapp.R
import com.example.basicapp.TaskApplication
import com.example.basicapp.data.RetrofitInstance
import com.example.basicapp.data.model.Task
import com.example.basicapp.data.worker.TaskReminderWorker
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.DateFormat.MEDIUM
import java.text.DateFormat.SHORT
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TaskDetailViewModel(application: Application) : AndroidViewModel(application) {

    private var _getTaskResponse = MutableSharedFlow<ApiResponse<Task>>()
    val getTaskResponse = _getTaskResponse

    private var _deleteTaskResponse = MutableSharedFlow<ApiResponse<Task>>()
    val deleteTaskResponse = _deleteTaskResponse

    private var _editTaskResponse = MutableSharedFlow<ApiResponse<Task>>()
    val editTaskResponse = _editTaskResponse

    fun editTask(id: Long, task: Task) {
        viewModelScope.launch {
            try {
                _editTaskResponse.emit(
                    ApiResponse.Success(
                        RetrofitInstance.api.editTask(id, task).body()
                    )
                )
            } catch (t:Throwable) {
                _editTaskResponse.emit(
                    ApiResponse.Error(t)
                )
            }
        }
    }

    fun getTask(id: Long) {
        viewModelScope.launch {
            try {
                _getTaskResponse.emit(
                    ApiResponse.Success(
                        RetrofitInstance.api.getTask(id).body()
                    )
                )
            } catch (t: Throwable) {
                _getTaskResponse.emit(
                    ApiResponse.Error(t)
                )
            }
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            try {
                _deleteTaskResponse.emit(
                    ApiResponse.Success(
                        RetrofitInstance.api.deleteTask(id).body()
                    )
                )
            } catch (t: Throwable) {
                _deleteTaskResponse.emit(
                    ApiResponse.Error(t)
                )
            }
        }
    }


    private val workManager = WorkManager.getInstance(application)

    fun cancelReminder(notificationTag: String) {
        workManager.cancelUniqueWork(notificationTag)
    }

    fun getPriority(priorityText: String): Int {
        val context = getApplication<TaskApplication>().applicationContext
        return when (priorityText) {
            context.getString(R.string.low_priority) -> 1
            context.getString(R.string.medium_priority) -> 2
            else -> 3
        }
    }

    val selectedLocation = MutableStateFlow<LatLng?>(null)

    fun setSelectedLocation(latitude: Double?, longitude: Double?) {
        viewModelScope.launch {
            selectedLocation.emit(
                LatLng(
                    latitude ?: return@launch,
                    longitude ?: return@launch
                )
            )
        }
    }

    fun formattedLocationText(geocoder: Geocoder): String? {
        selectedLocation.value?.let {
            val address = geocoder.getFromLocation(it.latitude, it.longitude, 1)
            if (address.isNullOrEmpty()) return "Unknown location"
            return "${address.first().thoroughfare} ${address.first().subThoroughfare},${address.first().locality}"
        }
        return null
    }

    val dateTimeVariables = MutableStateFlow<Map<String, Int>>(emptyMap())

    fun setDefaultDateTimeVariables(time: Long) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = time
            dateTimeVariables.emit(
                mapOf(
                    "year" to calendar.get(Calendar.YEAR),
                    "month" to calendar.get(Calendar.MONTH),
                    "dayOfMonth" to calendar.get(Calendar.DAY_OF_MONTH),
                    "hour" to calendar.get(Calendar.HOUR_OF_DAY),
                    "minute" to calendar.get(Calendar.MINUTE),
                )
            )
        }
    }

    fun setSelectedDateTimeVariables(
        selectedYear: Int? = null,
        selectedMonth: Int? = null,
        selectedDayOfMonth: Int? = null,
        selectedHour: Int? = null,
        selectedMinute: Int? = null,
    ) {
        viewModelScope.launch {
            dateTimeVariables.emit(
                mapOf(
                    "year" to (selectedYear ?: dateTimeVariables.value.getValue("year")),
                    "month" to (selectedMonth ?: dateTimeVariables.value.getValue("month")),
                    "dayOfMonth" to (selectedDayOfMonth
                        ?: dateTimeVariables.value.getValue("dayOfMonth")),
                    "hour" to (selectedHour ?: dateTimeVariables.value.getValue("hour")),
                    "minute" to (selectedMinute ?: dateTimeVariables.value.getValue("minute")),
                )
            )
        }
    }

    fun timeInMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(
            dateTimeVariables.value.getValue("year"),
            dateTimeVariables.value.getValue("month"),
            dateTimeVariables.value.getValue("dayOfMonth"),
            dateTimeVariables.value.getValue("hour"),
            dateTimeVariables.value.getValue("minute")
        )
        return calendar.timeInMillis
    }

    fun dateFormattedText(): String {
        return SimpleDateFormat.getDateInstance(MEDIUM).format(Date(timeInMillis()))
    }

    fun timeFormattedText(): String {
        return SimpleDateFormat.getTimeInstance(SHORT).format(Date(timeInMillis()))
    }

    fun scheduleReminder(
        notificationTag: String,
        selectedTimeInMillis: Long
    ) {

        val data = Data.Builder()
        data.putString(TaskReminderWorker.nameKey, notificationTag)


        val notificationRequest = OneTimeWorkRequest.Builder(TaskReminderWorker::class.java)
            .addTag("notification")
            .setInitialDelay(
                (selectedTimeInMillis - System.currentTimeMillis()),
                TimeUnit.MILLISECONDS
            )
            .setInputData(data.build())
            .build()

        workManager.enqueueUniqueWork(
            notificationTag,
            ExistingWorkPolicy.REPLACE,
            notificationRequest
        )

    }

}

class TaskDetailViewModelFactory(val application: Application) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskDetailViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}