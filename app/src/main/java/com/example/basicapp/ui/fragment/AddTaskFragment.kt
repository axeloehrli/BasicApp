package com.example.basicapp.ui.fragment

import android.Manifest
import android.app.*
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.example.basicapp.*
import com.example.basicapp.databinding.FragmentAddTaskBinding
import java.util.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.basicapp.data.model.Task
import com.example.basicapp.ui.viewmodel.*
import com.example.basicapp.util.addGeofence
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class AddTaskFragment : Fragment(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private lateinit var binding: FragmentAddTaskBinding

    private val runningQOrLater =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    private val viewModel: AddTaskViewModel by activityViewModels {
        AddTaskViewModelFactory(
            activity?.application as TaskApplication
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): (View) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_task, container, false)

        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedLocation.collect {
                    binding.locationEditText.setText(
                        viewModel.locationFormattedText(Geocoder(requireContext()))
                    )
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dateVariables.collect {
                    binding.dateEditText.setText(viewModel.formattedDateText())
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.timeVariables.collect {
                    binding.timeEditText.setText(viewModel.formattedTimeText())
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addTaskResponse.collect { apiResponse ->
                    when (apiResponse) {
                        is ApiResponse.Success -> {

                            viewModel.scheduleReminder(
                                binding.titleEditText.text.toString(),
                                viewModel.getTimeInMillis()
                            )

                            val geofencingClient =
                                LocationServices.getGeofencingClient(requireContext())

                            viewModel.selectedLocation.value.let { selectedLocation ->
                                selectedLocation ?: return@let
                                addGeofence(
                                    requireContext(),
                                    geofencingClient,
                                    selectedLocation,
                                    binding.titleEditText.text.toString()
                                )
                            }

                            findNavController().navigateUp()

                        }
                        is ApiResponse.Error -> {
                            Log.d("task", apiResponse.error.toString())
                            Snackbar.make(
                                binding.coordinatorLayout,
                                "There was a network error",
                                LENGTH_LONG
                            ).setAction("Ok") {}.show()
                        }
                    }
                }
            }
        }

        binding.apply {
            dateEditText.setOnClickListener {
                DatePickerDialog(
                    requireContext(),
                    this@AddTaskFragment,
                    viewModel.dateVariables.value.getValue("year"),
                    viewModel.dateVariables.value.getValue("month"),
                    viewModel.dateVariables.value.getValue("dayOfMonth")
                ).show()
            }

            timeEditText.setOnClickListener {
                TimePickerDialog(
                    requireContext(),
                    this@AddTaskFragment,
                    viewModel.timeVariables.value.getValue("hour"),
                    viewModel.timeVariables.value.getValue("minute"),
                    false
                ).show()
            }


            locationEditText.setOnClickListener {
                checkPermissions()
            }

            addButton.setOnClickListener {
                if (isEntryValid()) {
                    viewModel.addTask(
                        Task(
                            0,
                            viewModel.getPriority(
                                binding.autoCompleteTextView.text.toString()
                            ),
                            binding.titleEditText.text.toString(),
                            binding.titleEditText.text.toString(),
                            binding.descriptionEditText.text.toString(),
                            viewModel.getTimeInMillis(),
                            viewModel.selectedLocation.value?.latitude,
                            viewModel.selectedLocation.value?.longitude,
                        )
                    )
                }
            }
            val priorities = listOf(
                getString(R.string.low_priority),
                getString(R.string.medium_priority),
                getString(R.string.high_priority),
            )
            val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu_item, priorities)
            binding.autoCompleteTextView.setText(
                getString(R.string.low_priority)
            )
            binding.autoCompleteTextView.setAdapter(arrayAdapter)
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        viewModel.setDateVariables(year, month, dayOfMonth)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        viewModel.setTimeVariables(hourOfDay, minute)
    }

    private fun isEntryValid(): Boolean {
        return viewModel.isEntryValid(
            binding.titleEditText.text.toString(),
            binding.descriptionEditText.text.toString()
        )
    }

    private fun navigateToMapFragment() {
        val action = AddTaskFragmentDirections.actionAddTaskFragmentToMapsFragment()
        findNavController().navigate(action)
    }

    private fun checkPermissions() {
        if (isForegroundLocationPermissionGranted()) {
            if (runningQOrLater) {
                if (isBackgroundLocationPermissionGranted()) {
                    navigateToMapFragment()
                } else {
                    requestBackgroundLocationPermission()
                }
            } else {
                navigateToMapFragment()
            }
        } else {
            requestForegroundPermission()
        }
    }

    private fun isForegroundLocationPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestForegroundPermission() {
        requestForegroundPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private val requestForegroundPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permissionGranted ->
        when {
            // Permission granted
            permissionGranted -> {
                checkPermissions()
            }
            // Permission denied
            shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                Snackbar.make(
                    binding.coordinatorLayout,
                    "This feature requires location permission",
                    LENGTH_INDEFINITE
                ).setAction("Ok") {
                    checkPermissions()
                }.show()
            }
            // Permission permanently denied
            else -> {
                Snackbar.make(
                    binding.coordinatorLayout,
                    "Please enable location permission from your settings to use this feature",
                    LENGTH_INDEFINITE
                ).setAction("Ok") {}.show()
            }
        }
    }

    private fun isBackgroundLocationPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestBackgroundLocationPermission() {
        requestBackgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    private val requestBackgroundPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permissionGranted ->
        when {
            // Permission granted
            permissionGranted -> {
                checkPermissions()
            }
            // Permission denied
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION) -> {
                Snackbar.make(
                    binding.coordinatorLayout,
                    "This feature requires location to be accessed at all times",
                    LENGTH_INDEFINITE
                ).setAction("Ok") {
                    checkPermissions()
                }.show()
            }
            // Permission permanently denied
            else -> {
                Snackbar.make(
                    binding.coordinatorLayout,
                    "Please enable location permission from your settings",
                    LENGTH_INDEFINITE
                ).setAction("Ok") {}.show()
            }
        }
    }
}
