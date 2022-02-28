package com.example.basicapp.ui.fragments

import android.annotation.SuppressLint
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.basicapp.R
import com.example.basicapp.TaskApplication
import com.example.basicapp.databinding.FragmentMapsBinding
import com.example.basicapp.util.addGeofence
import com.example.basicapp.util.isNetworkAvailable
import com.example.basicapp.util.removeGeofence
import com.example.basicapp.ui.viewmodels.TaskViewModel
import com.example.basicapp.ui.viewmodels.TaskViewModelFactory
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@SuppressLint("MissingPermission")
class MapsFragment : Fragment() {

    private val viewModel: TaskViewModel by activityViewModels {
        TaskViewModelFactory(
            (activity?.application as TaskApplication).database.taskDao(),
            activity?.application as TaskApplication
        )
    }

    private lateinit var binding: FragmentMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var myLocation: LatLng
    private lateinit var map: GoogleMap

    var instructionsDialogShown = false

    private lateinit var locationCallback: LocationCallback

    private val mapCallback = OnMapReadyCallback { googleMap ->
        map = googleMap

        map.setOnMapLongClickListener { location ->
            if (isNetworkAvailable(requireContext())) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Location")
                    .setMessage("Do you want to add this task location?")
                    .setPositiveButton("Ok") { _, _ ->
                        viewModel.setLocation(location)
                        Toast.makeText(
                            requireContext(),
                            viewModel.locationFormattedText(requireContext()),
                            Toast.LENGTH_LONG
                        ).show()
                        findNavController().navigateUp()
                    }
                    .setNegativeButton("Cancel") { _, _ -> }
                    .show()

            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Network not available")
                    .setMessage("Please try again later")
                    .setPositiveButton("Ok") { _, _ ->
                    }.show()
            }
        }

        map.setOnMapClickListener { location ->
            addGeofence(
                requireContext(),
                geofencingClient,
                location,
                "Hello",
                map
            )
        }

        binding.buttonDelete.setOnClickListener {
            removeGeofence(requireContext(), geofencingClient, "geofence")
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location ?: return@addOnSuccessListener
                myLocation = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 20f))
                map.setOnCameraIdleListener {
                    if (!instructionsDialogShown) {
                        showInstructionsDialog()
                        instructionsDialogShown = true
                    }
                }
            }

        map.isMyLocationEnabled = true

    }

    private fun showInstructionsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Location picker")
            .setMessage("Select a location for your task by holding click anywhere in the map!")
            .setPositiveButton("Got it!") {_,_ ->}
            .show()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        geofencingClient = LocationServices.getGeofencingClient(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        binding = FragmentMapsBinding.inflate(inflater, container, false)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations){
                    // Update UI with location data
                    // ...

                }
            }
        }

        startLocationUpdates()

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(mapCallback)
    }

    override fun onStart() {
        super.onStart()
        startLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

}