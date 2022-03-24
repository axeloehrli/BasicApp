package com.example.basicapp.ui.fragment

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.basicapp.R
import com.example.basicapp.TaskApplication
import com.example.basicapp.databinding.FragmentEditTaskMapsBinding
import com.example.basicapp.ui.viewmodel.TaskDetailViewModel
import com.example.basicapp.ui.viewmodel.TaskDetailViewModelFactory
import com.example.basicapp.util.isNetworkAvailable
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@SuppressLint("MissingPermission")
class EditTaskMapsFragment : Fragment() {
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var binding : FragmentEditTaskMapsBinding
    private lateinit var map : GoogleMap

    private val viewModel : TaskDetailViewModel by activityViewModels {
        TaskDetailViewModelFactory(
            activity?.application as TaskApplication
        )
    }

    private var instructionsDialogShown = false
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        map.setOnMapLongClickListener { location ->
            if (isNetworkAvailable(requireContext())) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Location")
                    .setMessage("Do you want to add this task location?")
                    .setPositiveButton("Ok") { _, _ ->
                        viewModel.setSelectedLocation(location.latitude, location.longitude)
                        findNavController().navigateUp()
                    }
                    .setNegativeButton("Cancel") { _, _ -> }
                    .show()

            } else {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Network error")
                    .setMessage("Please try again later")
                    .setPositiveButton("Ok") { _, _ ->
                    }.show()
            }
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val myLocation = LatLng(location.latitude, location.longitude)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 20F))
            map.setOnCameraIdleListener {
                if (!instructionsDialogShown) {
                    showInstructionsDialog()
                    instructionsDialogShown = true
                }
            }
            map.isMyLocationEnabled = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditTaskMapsBinding.inflate(inflater,container,false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        geofencingClient = LocationServices.getGeofencingClient(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun showInstructionsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Location picker")
            .setMessage("Select a location for your task by holding click anywhere in the map!")
            .setPositiveButton("Got it!") {_,_ ->}
            .show()
    }
}