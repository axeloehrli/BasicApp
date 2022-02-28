package com.example.basicapp.ui.viewmodel

import android.app.Application
import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.*
import androidx.work.*
import com.example.basicapp.data.room.TaskDao
import com.example.basicapp.data.model.Task
import com.example.basicapp.data.worker.TaskReminderWorker
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import java.text.DateFormat.MEDIUM
import java.text.DateFormat.SHORT
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TaskViewModel(private val taskDao: TaskDao, application: Application) : ViewModel() {

    val allTasks: LiveData<List<Task>> = taskDao.getItems().asLiveData()

    private val workManager = WorkManager.getInstance(application)


    fun scheduleReminder(
        notificationTag: String,
        taskTitle: String,
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


    fun cancelReminder(notificationTag: String) {
        workManager.cancelUniqueWork(notificationTag)
    }


    private fun insertItem(task: Task) {
        viewModelScope.launch {
            taskDao.insert(task)
        }
    }

    fun addNewItem(
        title: String,
        description: String,
        time: Long,
        latitude: Double?,
        longitude: Double?
    ) {
        val newItem = Task(
            status = "Upcoming",
            notificationTag = title,
            title = title,
            description = description,
            time = time,
            latitude = latitude,
            longitude = longitude
        )
        insertItem(newItem)
    }


    fun retrieveItem(id: Int): LiveData<Task> {
        return taskDao.getItem(id).asLiveData()
    }

    fun deleteItem(task: Task) {
        viewModelScope.launch {
            taskDao.delete(task)
        }
    }

    fun isEntryValid(taskTitle: String, taskDescription: String): Boolean {
        if (taskTitle.isBlank() || taskDescription.isBlank()) {
            return false
        }
        return true
    }

    private fun updateItem(task: Task) {
        viewModelScope.launch {
            taskDao.update(task)
        }
    }

    fun editItem(
        id: Int,
        notificationTag: String,
        status: String,
        title: String,
        description: String,
        time: Long,
        latitude: Double?,
        longitude: Double?
    ) {
        val newTask = Task(
            id = id,
            notificationTag = notificationTag,
            status = status,
            title = title,
            description = description,
            time = time,
            latitude = latitude,
            longitude = longitude
        )

        updateItem(newTask)
    }


    private var _selectedLocation = MutableLiveData<LatLng?>()
    val selectedLocation: LiveData<LatLng?>
        get() = _selectedLocation


    fun setLocation(location: LatLng) {
        _selectedLocation.value = location
    }


    fun resetLocation() {
        _selectedLocation.value = null
    }

    fun locationFormattedText(context: Context): String? {
        _selectedLocation.value?.let {
            val address = Geocoder(context).getFromLocation(
                it.latitude,
                it.longitude,
                1
            )
            if (address.isNullOrEmpty()) return "Unknown location"
            return "${address.first().thoroughfare} ${address.first().subThoroughfare},${address.first().locality}"
        }
        return null
    }

    private var _selectedYear = MutableLiveData<Int?>()
    val selectedYear: LiveData<Int?>
        get() = _selectedYear

    private var _selectedMonth = MutableLiveData<Int?>()
    val selectedMonth: LiveData<Int?>
        get() = _selectedMonth

    private var _selectedDayOfMonth = MutableLiveData<Int?>()
    val selectedDayOfMonth: LiveData<Int?>
        get() = _selectedDayOfMonth

    private var _selectedHour = MutableLiveData<Int?>()
    val selectedHour: LiveData<Int?>
        get() = _selectedHour

    private var _selectedMinute = MutableLiveData<Int?>()
    val selectedMinute: LiveData<Int?>
        get() = _selectedMinute

    private val calendar: Calendar = Calendar.getInstance()
    var defaultYear = calendar.get(Calendar.YEAR)
    var defaultMonth = calendar.get(Calendar.MONTH)
    var defaultDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    var defaultHour = calendar.get(Calendar.HOUR_OF_DAY)
    var defaultMinute = calendar.get(Calendar.MINUTE)


    fun updateDefaultDateTime(
        taskYear: Int? = null,
        taskMonth: Int? = null,
        taskDayOfMonth: Int? = null,
        taskHour: Int? = null,
        taskMinute: Int? = null,

        ) {
        val newCalendar = Calendar.getInstance()
        defaultYear = taskYear ?: newCalendar.get(Calendar.YEAR)
        defaultMonth = taskMonth ?: newCalendar.get(Calendar.MONTH)
        defaultDayOfMonth = taskDayOfMonth ?: newCalendar.get(Calendar.DAY_OF_MONTH)
        defaultHour = taskHour ?: newCalendar.get(Calendar.HOUR_OF_DAY)
        defaultMinute = taskMinute ?: newCalendar.get(Calendar.MINUTE)
    }

    fun setSelectedDate(year: Int, month: Int, dayOfMonth: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month
        _selectedDayOfMonth.value = dayOfMonth
    }

    fun setSelectedTime(hour: Int, minute: Int) {
        _selectedHour.value = hour
        _selectedMinute.value = minute
    }

    fun getDateTimeInMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(
            selectedYear.value ?: defaultYear,
            selectedMonth.value ?: defaultMonth,
            selectedDayOfMonth.value ?: defaultDayOfMonth,
            selectedHour.value ?: defaultHour,
            selectedMinute.value ?: defaultMinute
        )
        return calendar.timeInMillis
    }

    fun dateFormattedText(): String {
        return SimpleDateFormat.getDateInstance(MEDIUM).format(Date(getDateTimeInMillis()))
    }

    fun timeFormattedText(): String {
        return SimpleDateFormat.getTimeInstance(SHORT).format(Date(getDateTimeInMillis()))
    }

    fun resetDateTime() {
        _selectedYear.value = null
        _selectedMonth.value = null
        _selectedDayOfMonth.value = null
        _selectedHour.value = null
        _selectedMinute.value = null
    }


}


class TaskViewModelFactory(private val taskDao: TaskDao, val application: Application) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(taskDao, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

