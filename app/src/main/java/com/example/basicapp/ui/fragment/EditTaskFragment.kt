package com.example.basicapp.ui.fragment

import android.app.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.basicapp.*
import com.example.basicapp.data.model.Task
import com.example.basicapp.databinding.FragmentEditTaskBinding
import com.example.basicapp.ui.viewmodel.TaskViewModel
import com.example.basicapp.ui.viewmodel.TaskViewModelFactory
import com.example.basicapp.util.addGeofence
import com.example.basicapp.util.removeGeofence
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.util.*

class EditTaskFragment : Fragment(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private val viewModel: TaskViewModel by activityViewModels {
        TaskViewModelFactory(
            (activity?.application as TaskApplication).database.taskDao(),
            activity?.application as TaskApplication
        )
    }

    private lateinit var binding: FragmentEditTaskBinding

    private val navigationArgs: EditTaskFragmentArgs by navArgs()

    private lateinit var task: Task

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


        val id = navigationArgs.itemId
        viewModel.retrieveItem(id).observe(viewLifecycleOwner) {

            task = it

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = task.time

            viewModel.updateDefaultDateTime(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
            )

            setDefaultLocation()

            bind(it)

            bindEditTexts()

        }
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
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetDateTime()
        viewModel.resetLocation()
    }

    private fun setDefaultLocation() {
        if (viewModel.selectedLocation.value != null) return
        viewModel.setLocation(
            LatLng(
                task.latitude ?: return,
                task.longitude ?: return
            )
        )
    }

    private fun bind(task: Task) {
        binding.apply {
            titleEditText.setText(task.title)
            descriptionEditText.setText(task.description)
            root.setOnClickListener {
                bindEditTexts()
            }
            binding.dateEditText.setOnClickListener {
                DatePickerDialog(
                    requireContext(),
                    this@EditTaskFragment,
                    viewModel.selectedYear.value ?: viewModel.defaultYear,
                    viewModel.selectedMonth.value ?: viewModel.defaultMonth,
                    viewModel.selectedDayOfMonth.value ?: viewModel.defaultDayOfMonth
                ).show()
            }

            binding.timeEditText.setOnClickListener {
                TimePickerDialog(
                    requireContext(),
                    this@EditTaskFragment,
                    viewModel.selectedHour.value ?: viewModel.defaultHour,
                    viewModel.selectedMinute.value ?: viewModel.defaultMinute,
                    false
                ).show()
            }

            locationEditText.setOnClickListener {
                val action = EditTaskFragmentDirections.actionEditTaskFragmentToMapsFragment()
                findNavController().navigate(action)
            }

            editButton.setOnClickListener {
                viewModel.scheduleReminder(
                    task.notificationTag,
                    viewModel.getDateTimeInMillis()
                )
                viewModel.selectedLocation.value?.let { selectedLocation ->
                    val geofencingClient = LocationServices.getGeofencingClient(requireContext())
                    removeGeofence(
                        requireContext(),
                        geofencingClient,
                        task.title
                    )
                    addGeofence(
                        requireContext(),
                        geofencingClient,
                        selectedLocation,
                        binding.titleEditText.text.toString()
                    )
                }
                editItem()
            }
        }
    }

    private fun editItem() {
        viewModel.editItem(
            task.id,
            task.notificationTag,
            "Upcoming",
            binding.titleEditText.text.toString(),
            binding.descriptionEditText.text.toString(),
            viewModel.getDateTimeInMillis(),
            viewModel.selectedLocation.value?.latitude ?: task.latitude,
            viewModel.selectedLocation.value?.longitude ?: task.latitude
        )

        findNavController().navigateUp()
    }

    override fun onDateSet(
        view: DatePicker?,
        year: Int,
        month: Int,
        dayOfMonth: Int
    ) {
        viewModel.setSelectedDate(
            year,
            month,
            dayOfMonth
        )
        bindEditTexts()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        viewModel.setSelectedTime(
            hourOfDay,
            minute
        )
        bindEditTexts()
    }
}


