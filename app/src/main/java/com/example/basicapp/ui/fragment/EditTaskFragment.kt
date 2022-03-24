package com.example.basicapp.ui.fragment

import android.app.*
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.basicapp.*
import com.example.basicapp.data.model.*
import com.example.basicapp.databinding.FragmentEditTaskBinding
import com.example.basicapp.ui.viewmodel.*
import com.example.basicapp.util.addGeofence
import com.example.basicapp.util.removeGeofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.util.*

class EditTaskFragment : Fragment(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {


    private val taskDetailViewModel: TaskDetailViewModel by activityViewModels {
        TaskDetailViewModelFactory(
            activity?.application as TaskApplication
        )
    }

    private lateinit var binding: FragmentEditTaskBinding

    private val navigationArgs: EditTaskFragmentArgs by navArgs()


    // inflates layout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_task, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.locationEditText.setOnClickListener {
            val action = EditTaskFragmentDirections.actionEditTaskFragmentToEditTaskMapsFragment()
            findNavController().navigate(action)
        }
        taskDetailViewModel.getTask(navigationArgs.itemId)
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                taskDetailViewModel.getTaskResponse.collect { apiResponse ->
                    Log.d("task", apiResponse.toString())
                    when (apiResponse) {
                        is ApiResponse.Success -> {
                            apiResponse.data?.let { task ->
                                if (taskDetailViewModel.dateTimeVariables.value.isEmpty()) {
                                    taskDetailViewModel.setDefaultDateTimeVariables(
                                        task.time
                                    )
                                    taskDetailViewModel.setSelectedLocation(
                                        task.latitude,
                                        task.longitude
                                    )
                                }
                                bind(task)
                            }
                        }
                        is ApiResponse.Error -> {
                            Snackbar.make(
                                binding.coordinatorLayout,
                                "There was a network error",
                                LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                taskDetailViewModel.dateTimeVariables.collect { variables ->
                    if (variables.isEmpty()) return@collect
                    binding.dateEditText.setText(
                        taskDetailViewModel.dateFormattedText()
                    )
                    binding.timeEditText.setText(
                        taskDetailViewModel.timeFormattedText()
                    )
                    binding.dateEditText.setOnClickListener {
                        DatePickerDialog(
                            requireContext(),
                            this@EditTaskFragment,
                            variables.getValue("year"),
                            variables.getValue("month"),
                            variables.getValue("dayOfMonth"),
                        ).show()
                    }
                    binding.timeEditText.setOnClickListener {
                        TimePickerDialog(
                            requireContext(),
                            this@EditTaskFragment,
                            variables.getValue("hour"),
                            variables.getValue("minute"),
                            false
                        ).show()
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                taskDetailViewModel.selectedLocation.collect {
                    binding.locationEditText.setText(
                        taskDetailViewModel.formattedLocationText(Geocoder(requireContext()))
                    )
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            taskDetailViewModel.editTaskResponse.collect { apiResponse ->
                when (apiResponse) {
                    is ApiResponse.Success -> {
                        Log.d("task", apiResponse.data.toString())
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


    override fun onDestroy() {
        super.onDestroy()
        taskDetailViewModel.dateTimeVariables.value = emptyMap()
        taskDetailViewModel.selectedLocation.value = null
    }

    private fun bind(task: Task) {
        Log.d("task", "BOUND")
        binding.apply {
            titleEditText.setText(task.title)
            descriptionEditText.setText(task.description)
            dateEditText.setText(task.getFormattedDate())
            timeEditText.setText(task.getFormattedTime())

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

            locationEditText.setText(
                taskDetailViewModel.formattedLocationText(Geocoder(requireContext()))
                    ?: task.getFormattedLocation(Geocoder(requireContext()))
            )
            editButton.setOnClickListener {
                taskDetailViewModel.editTask(
                    navigationArgs.itemId,
                    Task(
                        0,
                        taskDetailViewModel.getPriority(binding.autoCompleteTextView.text.toString()),
                        task.notificationTag,
                        binding.titleEditText.text.toString(),
                        binding.descriptionEditText.text.toString(),
                        taskDetailViewModel.timeInMillis(),
                        taskDetailViewModel.selectedLocation.value?.latitude,
                        taskDetailViewModel.selectedLocation.value?.longitude,
                    )
                )
                taskDetailViewModel.scheduleReminder(
                    task.notificationTag,
                    taskDetailViewModel.timeInMillis()
                )
                removeGeofence(
                    requireContext(),
                    GeofencingClient(requireContext()),
                    task.title
                )
                taskDetailViewModel.selectedLocation.value?.let { selectedLocation ->
                    addGeofence(
                        requireContext(),
                        GeofencingClient(requireContext()),
                        LatLng(
                            selectedLocation.latitude,
                            selectedLocation.longitude
                        ),
                        binding.titleEditText.text.toString()
                    )
                    return@setOnClickListener
                }
                if (task.latitude != null && task.longitude != null) {
                    addGeofence(
                        requireContext(),
                        GeofencingClient(requireContext()),
                        LatLng(
                            task.latitude,
                            task.longitude
                        ),
                        binding.titleEditText.text.toString()
                    )
                }
            }
        }
    }

    override fun onDateSet(
        view: DatePicker?,
        year: Int,
        month: Int,
        dayOfMonth: Int
    ) {
        taskDetailViewModel.setSelectedDateTimeVariables(
            selectedYear = year,
            selectedMonth = month,
            selectedDayOfMonth = dayOfMonth
        )
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        taskDetailViewModel.setSelectedDateTimeVariables(
            selectedHour = hourOfDay,
            selectedMinute = minute,
        )
    }
}


