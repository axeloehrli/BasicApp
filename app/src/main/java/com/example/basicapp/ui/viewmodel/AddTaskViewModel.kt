package com.example.basicapp.ui.viewmodel

import android.app.Application
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.*
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.text.DateFormat.MEDIUM
import java.text.DateFormat.SHORT
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AddTaskViewModel(application: Application) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)

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

    private val calendar: Calendar = Calendar.getInstance()

    private var _addTaskResponse = MutableSharedFlow<ApiResponse<Task>>()
    val addTaskResponse = _addTaskResponse


    fun addTask(task: Task) {
        viewModelScope.launch {
            try {
                _addTaskResponse.emit(
                    ApiResponse.Success(
                        RetrofitInstance.api.addTask(task).body()
                    )
                )
            } catch (t: Throwable) {
                _addTaskResponse.emit(
                    ApiResponse.Error(t)
                )
            }
        }
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

    fun setSelectedLocation(location: LatLng) {
        viewModelScope.launch {
            selectedLocation.emit(location)
        }
    }

    fun locationFormattedText(geocoder: Geocoder): String? {
        selectedLocation.value?.let {
            val address = geocoder.getFromLocation(
                it.latitude,
                it.longitude,
                1
            )
            if (address.isNullOrEmpty()) return "Unknown location"
            return "${address.first().thoroughfare} ${address.first().subThoroughfare},${address.first().locality}"
        }
        return null
    }

    val dateVariables = MutableStateFlow(
        mapOf(
            "year" to calendar.get(Calendar.YEAR),
            "month" to calendar.get(Calendar.MONTH),
            "dayOfMonth" to calendar.get(Calendar.DAY_OF_MONTH)
        )
    )

    val timeVariables = MutableStateFlow(
        mapOf(
            "hour" to calendar.get(Calendar.HOUR_OF_DAY),
            "minute" to calendar.get(Calendar.MINUTE),
        )
    )


    fun setDateVariables(selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int) {
        viewModelScope.launch {
            dateVariables.emit(
                mapOf(
                    "year" to selectedYear,
                    "month" to selectedMonth,
                    "dayOfMonth" to selectedDayOfMonth
                )
            )
        }
    }

    fun setTimeVariables(selectedHour: Int, selectedMinute: Int) {
        viewModelScope.launch {
            timeVariables.emit(
                mapOf(
                    "hour" to selectedHour,
                    "minute" to selectedMinute
                )
            )
        }
    }

    fun getTimeInMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(
            dateVariables.value.getValue("year"),
            dateVariables.value.getValue("month"),
            dateVariables.value.getValue("dayOfMonth"),
            timeVariables.value.getValue("hour"),
            timeVariables.value.getValue("minute")
        )
        return calendar.timeInMillis
    }

    fun formattedDateText(): String {
        return SimpleDateFormat.getDateInstance(MEDIUM).format(Date(getTimeInMillis()))
    }

    fun formattedTimeText(): String {
        return SimpleDateFormat.getTimeInstance(SHORT).format(Date(getTimeInMillis()))
    }

    fun isEntryValid(title: String, description: String): Boolean {
        return title != "" && description != ""
    }
}

class AddTaskViewModelFactory(val application: Application) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddTaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddTaskViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}