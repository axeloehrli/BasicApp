package com.example.basicapp.util

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.basicapp.data.receiver.GeofenceBroadcastReceiver
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


fun addGeofence(
    context: Context,
    geofencingClient: GeofencingClient,
    location: LatLng,
    taskTitle: String = "Hello",
    map: GoogleMap? = null
) {
    val geofence = Geofence.Builder()
        .setRequestId(taskTitle)
        .setCircularRegion(location.latitude, location.longitude, 20f)
        .setExpirationDuration(Geofence.NEVER_EXPIRE)
        .setLoiteringDelay(3000)
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER )
        .build()

    val geofencingRequest =
        GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER )
            addGeofence(geofence)
        }

    val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        intent.putExtra("geofenceId", taskTitle)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        geofencingClient.addGeofences(geofencingRequest.build(), geofencePendingIntent)
            .addOnSuccessListener {

                map?.addMarker(MarkerOptions().position(location).title("HELLO"))

                val geocoder = Geocoder(context)

                val markerTitle = when {
                    geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        .isEmpty() -> "Unknown Location"
                    else -> {
                        "${
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                .first().thoroughfare ?: null
                        }${
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                .first().subThoroughfare ?: null
                        }${
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                .first().locality
                        }, ${
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                .first().subAdminArea
                        }, ${
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                .first().adminArea
                        }"
                    }
                }

                Toast.makeText(context, markerTitle, Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Log.d("task", it.toString())
            }
        return
    }
}

fun hello() {
    val one = 1
    val two = 2
}

fun removeGeofence(context: Context, geofencingClient: GeofencingClient, geofenceId: String) {
    geofencingClient.removeGeofences(listOf(geofenceId))
        .addOnSuccessListener {
            Toast.makeText(context, "GEOFENCE $geofenceId REMOVED!", Toast.LENGTH_LONG).show()
        }.addOnFailureListener {
            Toast.makeText(context, "GEOFENCE REMOVAL FAILED! $it", Toast.LENGTH_LONG)
                .show()
        }

}



