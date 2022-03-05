package com.example.basicapp.ui.fragment

import android.Manifest
import android.app.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.example.basicapp.*
import com.example.basicapp.databinding.FragmentAddTaskBinding
import java.util.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.basicapp.ui.viewmodel.TaskViewModel
import com.example.basicapp.ui.viewmodel.TaskViewModelFactory
import com.example.basicapp.util.addGeofence
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar


class AddTaskFragment : Fragment(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private lateinit var binding: FragmentAddTaskBinding

    private val runningQOrLater =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    private val viewModel: TaskViewModel by activityViewModels {
        TaskViewModelFactory(
            activity?.application as TaskApplication
        )
    }

    // inflates layout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): (View) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_task, container, false)

        return (binding.root)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.dateEditText.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                this,
                viewModel.selectedYear.value ?: viewModel.defaultYear,
                viewModel.selectedMonth.value ?: viewModel.defaultMonth,
                viewModel.selectedDayOfMonth.value ?: viewModel.defaultDayOfMonth
            ).show()
        }

        binding.timeEditText.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                this,
                viewModel.selectedHour.value ?: viewModel.defaultHour,
                viewModel.selectedMinute.value ?: viewModel.defaultMinute,
                false
            ).show()
        }

        // creates a time picker dialog with the time variables

        binding.locationEditText.setOnClickListener {
            checkPermissions()
        }

        binding.addButton.setOnClickListener {
            if (isEntryValid()) {
                viewModel.scheduleReminder(
                    binding.titleEditText.text.toString(),
                    viewModel.getTaskDateTimeInMillis()
                )

                val geofencingClient = LocationServices.getGeofencingClient(requireContext())

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
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateDefaultDateTime()
        viewModel.setDefaultPriority(
            resources.getString(R.string.low_priority)
        )
        bindEditTexts()

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetSelectedDateTime()
        viewModel.resetSelectedLocation()
        viewModel.resetSelectedPriority()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        viewModel.setSelectedDate(year, month, dayOfMonth)
        bindEditTexts()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        viewModel.setSelectedTime(hourOfDay, minute)
        bindEditTexts()
    }


    private fun isEntryValid(): Boolean {
        return viewModel.isEntryValid(
            binding.titleEditText.text.toString(),
            binding.descriptionEditText.text.toString()
        )
    }

    private fun bindEditTexts() {
        binding.dateEditText.setText(
            viewModel.dateFormattedText()
        )

        binding.timeEditText.setText(
            viewModel.timeFormattedText()
        )

        binding.locationEditText.setText(
            viewModel.locationFormattedText(requireContext())
        )

        val taskPriorities = listOf(
            resources.getString(R.string.low_priority),
            resources.getString(R.string.medium_priority),
            resources.getString(R.string.high_priority)
        )
        val arrayAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_menu_item, taskPriorities)
        binding.autoCompleteTextView.setAdapter(arrayAdapter)
        binding.autoCompleteTextView.setText(
            viewModel.selectedPriority.value ?:
            viewModel.defaultPriority.value,
            false
        )
        binding.autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            viewModel.setSelectedPriority(position)
        }

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
