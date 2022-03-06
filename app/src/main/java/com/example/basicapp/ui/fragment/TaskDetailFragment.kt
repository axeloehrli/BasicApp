package com.example.basicapp.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.basicapp.R
import com.example.basicapp.TaskApplication
import com.example.basicapp.data.model.ApiResponse
import com.example.basicapp.data.model.Task
import com.example.basicapp.data.model.getFormattedDateAndTime
import com.example.basicapp.databinding.FragmentTaskDetailBinding
import com.example.basicapp.util.removeGeofence
import com.example.basicapp.ui.viewmodel.TaskViewModel
import com.example.basicapp.ui.viewmodel.TaskViewModelFactory
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TaskDetailFragment : Fragment() {

    private val viewModel: TaskViewModel by activityViewModels {
        TaskViewModelFactory(
            activity?.application as TaskApplication
        )
    }

    private val navigationArgs: TaskDetailFragmentArgs by navArgs()

    private lateinit var binding: FragmentTaskDetailBinding

    private lateinit var task: Task

    private fun bind(task: Task) {
        binding.apply {
            itemTitle.text = task.title
            itemDescription.text = task.description
            itemDate.text = task.getFormattedDateAndTime()

            buttonDelete.setOnClickListener {
                showConfirmationDialog()
            }

            buttonEdit.setOnClickListener {
                editItem()
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
        viewModel.getTask(navigationArgs.itemId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getTaskResponse.collectLatest { response ->
                if (response.error) {
                    Log.d("task", "Error loading task")
                } else {
                    response.body?.let {
                        Log.d("task", "Task successfully loaded ")
                        bind(it)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteTaskResponse.collectLatest { response ->
                if (response.error) {
                    Log.d("task", "Could not delete task")
                } else {
                    Log.d("task", "Task successfully deleted")
                    val action = TaskDetailFragmentDirections.actionTaskDetailFragmentToFragmentAllTasks()
                    findNavController().navigate(action)
                }
            }
        }


    }

    override fun onStop() {
        super.onStop()
    }

    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(android.R.string.dialog_alert_title))
            .setMessage("Do you want to delete this Task?")
            .setCancelable(false)
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteTask(navigationArgs.itemId)
                /*viewModel.cancelReminder(task.title)
                val geofencingClient = LocationServices.getGeofencingClient(requireContext())
                removeGeofence(
                    requireContext(),
                    geofencingClient,
                    task.title
                )*/
            }
            .show()
    }


    private fun editItem() {

        val action =
            TaskDetailFragmentDirections.actionTaskDetailFragmentToEditTaskFragment(task.id)
        findNavController().navigate(action)
    }
}