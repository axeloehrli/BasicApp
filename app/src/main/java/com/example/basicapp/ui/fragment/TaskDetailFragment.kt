package com.example.basicapp.ui.fragment

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.basicapp.R
import com.example.basicapp.TaskApplication
import com.example.basicapp.data.model.Task
import com.example.basicapp.data.model.getFormattedDateAndTime
import com.example.basicapp.data.model.getFormattedLocation
import com.example.basicapp.databinding.FragmentTaskDetailBinding
import com.example.basicapp.ui.viewmodel.*
import com.example.basicapp.util.removeGeofence
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TaskDetailFragment : Fragment() {

    private val taskDetailViewModel: TaskDetailViewModel by activityViewModels {
        TaskDetailViewModelFactory(
            activity?.application as TaskApplication
        )
    }

    private val navigationArgs: TaskDetailFragmentArgs by navArgs()

    private lateinit var binding: FragmentTaskDetailBinding


    private fun bind(task: Task) {
        binding.apply {
            taskTitle.text = task.title
            taskDescription.text = task.description
            taskDate.text = task.getFormattedDateAndTime()

            task.getFormattedLocation(Geocoder(requireContext()))?.let { locationText ->
                taskLocation.visibility = View.VISIBLE
                taskLocation.text = locationText
            }

            buttonDelete.setOnClickListener {
                showConfirmationDialog()
            }

            buttonEdit.setOnClickListener {
                val action = TaskDetailFragmentDirections.actionTaskDetailFragmentToEditTaskFragment(navigationArgs.itemId)
                findNavController().navigate(action)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        taskDetailViewModel.getTask(navigationArgs.itemId)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                taskDetailViewModel.getTaskResponse.collect { response ->
                    when (response) {
                        is ApiResponse.Success -> {
                            response.data?.let { task -> bind(task) }
                        }
                        is ApiResponse.Error -> {
                            Log.d("task", response.error.toString())
                            binding.root.visibility = View.GONE
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Network error")
                                .setMessage("Please try again later")
                                .setCancelable(false)
                                .setPositiveButton("Ok") { _, _ ->
                                    findNavController().navigateUp()
                                }.show()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                taskDetailViewModel.deleteTaskResponse.collect { response ->
                    when (response) {
                        is ApiResponse.Success -> {
                            response.data?.let { task ->
                                taskDetailViewModel.cancelReminder(task.title)
                                removeGeofence(
                                    requireContext(),
                                    LocationServices.getGeofencingClient(requireContext()),
                                    task.title
                                )
                            }
                            findNavController().navigateUp()
                        }
                        is ApiResponse.Error -> {
                            Log.d("task", response.error.toString())
                        }
                    }
                }
            }
        }
    }

    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(android.R.string.dialog_alert_title))
            .setMessage("Do you want to delete this Task?")
            .setCancelable(false)
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                taskDetailViewModel.deleteTask(navigationArgs.itemId)
            }
            .show()
    }
}