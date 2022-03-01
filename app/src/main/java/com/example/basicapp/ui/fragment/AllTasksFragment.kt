package com.example.basicapp.ui.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.basicapp.*
import com.example.basicapp.data.datastore.SettingsDataStore
import com.example.basicapp.databinding.FragmentAllTasksBinding
import com.example.basicapp.ui.viewmodel.TaskViewModel
import com.example.basicapp.ui.viewmodel.TaskViewModelFactory
import com.example.basicapp.ui.adapter.Adapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class AllTasksFragment : Fragment() {

    private val viewModel: TaskViewModel by activityViewModels {
        TaskViewModelFactory(
            (activity?.application as TaskApplication).database.taskDao(),
            activity?.application as TaskApplication
        )
    }

    private lateinit var binding: FragmentAllTasksBinding
    private lateinit var adapter: Adapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonAdd: FloatingActionButton

    private lateinit var SettingsDataStore: SettingsDataStore

    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): (View) {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_all_tasks, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.lifecycleOwner = this

        buttonAdd = binding.buttonAdd
        buttonAdd.setOnClickListener {
            val action = AllTasksFragmentDirections.actionFragmentAllTasksToAddTaskFragment()
            findNavController().navigate(action)
        }

        recyclerView = binding.recyclerView
        adapter = Adapter {
            val action =
                AllTasksFragmentDirections.actionFragmentAllTasksToTaskDetailFragment(it.id)
            findNavController().navigate(action)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        /*viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            adapter.submitList(tasks)

            binding.emptyMessage.visibility =
                if (tasks.isEmpty()) View.VISIBLE else View.GONE
        }*/


        SettingsDataStore = SettingsDataStore(requireContext())

        SettingsDataStore.sortOrderFlow.asLiveData().observe(viewLifecycleOwner) {
            sortOrder ->
            viewModel.sortedTasks(sortOrder).observe(viewLifecycleOwner) {
                tasks ->
                adapter.submitList(tasks)
                binding.emptyMessage.visibility =
                    if (tasks.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.sort_order_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.sort_by_date -> {
                lifecycleScope.launch {
                    SettingsDataStore.saveSortByToPreferences(
                        "sort_by_date",
                        requireContext()
                    )
                }
            }
            else -> {
                lifecycleScope.launch {
                    SettingsDataStore.saveSortByToPreferences(
                        "sort_by_none",
                        requireContext()
                    )
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
