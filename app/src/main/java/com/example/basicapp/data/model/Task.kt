package com.example.basicapp.data.model

import android.graphics.Color
import android.location.Geocoder
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.text.DateFormat.MEDIUM
import java.text.DateFormat.SHORT
import java.text.SimpleDateFormat
import java.util.*

data class Task(

    @SerializedName("ID")val id: Int = 0,

    @SerializedName("NotificationTag")var notificationTag: String,

    @SerializedName("Title")var title: String,

    @SerializedName("Description")var description: String,

    @SerializedName("Time")val time: Long,

    @SerializedName("Latitude")val latitude: Double?,

    @SerializedName("Longitude")val longitude: Double?

)


fun Task.getFormattedDateAndTime(): String =
    SimpleDateFormat.getDateTimeInstance(MEDIUM, SHORT).format(time)


fun Task.getFormattedTime(): String = SimpleDateFormat.getTimeInstance(SHORT).format(time)
fun Task.getFormattedMonth(): String = SimpleDateFormat("MMM", Locale.getDefault()).format(time)
fun Task.getFormattedDayOfMonth(): String = SimpleDateFormat("d", Locale.getDefault()).format(time)
fun Task.getFormattedDayOfWeek(): String = SimpleDateFormat("E", Locale.getDefault()).format(time)
fun Task.getFormattedLocation(geocoder: Geocoder): String? {
    val address = geocoder.getFromLocation(
        latitude ?: return null,
        longitude ?: return null,
        1
    )
    if (address.isEmpty()) return "Unknown location"
    return "${address.first().thoroughfare ?: return null} ${address.first().subThoroughfare ?: return null},${address.first().locality ?: return null}"
}
