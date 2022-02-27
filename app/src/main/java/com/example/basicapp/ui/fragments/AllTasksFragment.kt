package com.example.basicapp.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.basicapp.*
import com.example.basicapp.data.datastore.SettingsDataStore
import com.example.basicapp.databinding.FragmentAllTasksBinding
import com.example.basicapp.ui.viewmodels.TaskViewModel
import com.example.basicapp.ui.viewmodels.TaskViewModelFactory
import com.example.basicapp.ui.adapters.Adapter
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

    private var isLinearLayoutManager = true
    private var isDarkMode = false

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

        viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            adapter.submitList(tasks)

            binding.emptyMessage.visibility =
                if (tasks.isEmpty()) View.VISIBLE else View.GONE
        }


        SettingsDataStore = SettingsDataStore(requireContext())

        SettingsDataStore.preferenceFlow.asLiveData().observe(viewLifecycleOwner) {
            isLinearLayoutManager = it
            chooseLayout()
            activity?.invalidateOptionsMenu()
        }

        SettingsDataStore.backgroundFlow.asLiveData().observe(viewLifecycleOwner) {
            isDarkMode = it
            chooseBackground()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.layout_menu, menu)

        val layoutButton = menu.findItem(R.id.action_switch_layout)
        val darkModeButton = menu.findItem(R.id.action_dark_mode)
        setLayoutIcon(layoutButton)
        setDarkModeIcon(darkModeButton)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_switch_layout -> {
                // Sets isLinearLayoutManager (a Boolean) to the opposite value
                isLinearLayoutManager = !isLinearLayoutManager
                // Sets layout and icon

                lifecycleScope.launch {
                    SettingsDataStore.saveLayoutToPreferencesStore(
                        isLinearLayoutManager,
                        requireContext()
                    )
                }

                setLayoutIcon(item)

                return true
            }

            R.id.action_dark_mode -> {
                isDarkMode = !isDarkMode
                lifecycleScope.launch {
                    SettingsDataStore.saveBackgroundToPreferencesStore(
                        isDarkMode,
                        requireContext()
                    )
                }
                setDarkModeIcon(item)
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun chooseLayout() {
        if (isLinearLayoutManager) {
            recyclerView.layoutManager = LinearLayoutManager(context)
        } else {
            recyclerView.layoutManager = GridLayoutManager(context, 2)
        }
    }

    private fun setLayoutIcon(menuItem: MenuItem?) {
        if (menuItem == null)
            return

        menuItem.icon =
            if (isLinearLayoutManager)
                ContextCompat.getDrawable(this.requireContext(), R.drawable.ic_grid_layout)
            else ContextCompat.getDrawable(this.requireContext(), R.drawable.ic_linear_layout)
    }

    private fun chooseBackground() {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun setDarkModeIcon(menuItem: MenuItem?) {
        if (menuItem == null) return

        menuItem.icon =
            if (isDarkMode) {
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_light_mode_24)
            } else ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_dark_mode_24)

    }
}

