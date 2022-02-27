package com.example.basicapp.data.model

import android.location.Geocoder
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.DateFormat.MEDIUM
import java.text.DateFormat.SHORT
import java.text.SimpleDateFormat

@Entity(tableName = "task")
data class Task(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "notification_tag")
    var notificationTag: String,

    @ColumnInfo(name = "status")
    var status: String,

    @ColumnInfo(name = "title")
    var title: String,

    @ColumnInfo(name = "description")
    var description: String,

    @ColumnInfo(name = "time")
    val time: Long,

    @ColumnInfo(name = "latitude")
    val latitude: Double?,

    @ColumnInfo(name = "longitude")
    val longitude: Double?
)


fun Task.getFormattedDateAndTime(): String =
    SimpleDateFormat.getDateTimeInstance(MEDIUM, SHORT).format(time)

fun Task.getFormattedDate(): String = SimpleDateFormat.getDateInstance(MEDIUM).format(time)
fun Task.getFormattedTime(): String = SimpleDateFormat.getTimeInstance(SHORT).format(time)
fun Task.getFormattedMonth(): String = SimpleDateFormat("MMM").format(time)
fun Task.getFormattedDayOfMonth(): String = SimpleDateFormat("d").format(time)
fun Task.getFormattedDayOfWeek(): String = SimpleDateFormat("E").format(time)
fun Task.getFormattedLocation(geocoder: Geocoder): String? {
    if (latitude != null && longitude != null) {
        val address = geocoder.getFromLocation(
            latitude,
            longitude,
            1
        )
        if (address.isEmpty()) return "Unknown location"
        return "${address.first().thoroughfare} ${address.first().subThoroughfare},${address.first().locality}"
    }
    return null
}
