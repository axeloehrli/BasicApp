package com.example.basicapp.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.basicapp.*
import com.example.basicapp.data.datastore.SettingsDataStore
import com.example.basicapp.databinding.FragmentListBinding
import com.example.basicapp.ui.adapter.Adapter
import com.example.basicapp.ui.viewmodel.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    private val viewModel: ListViewModel by viewModels()

    private lateinit var binding: FragmentListBinding
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_list, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        SettingsDataStore = SettingsDataStore(requireContext())

        buttonAdd = binding.buttonAdd
        buttonAdd.setOnClickListener {
            val action = ListFragmentDirections.actionFragmentAllTasksToAddTaskFragment()
            findNavController().navigate(action)
        }

        recyclerView = binding.recyclerView
        adapter = Adapter { taskId ->
            val action =
                ListFragmentDirections.actionFragmentAllTasksToTaskDetailFragment(taskId)
            findNavController().navigate(action)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                SettingsDataStore.sortOrderFlow.collect { sortOrder ->
                    Log.d("task", sortOrder)
                    when (sortOrder) {
                        "sort_by_time" -> viewModel.getTasksByTime()
                        "sort_by_priority" -> viewModel.sortTasksByPriority()
                    }
                    binding.swipeToRefresh.setOnRefreshListener {
                        when (sortOrder) {
                            "sort_by_time" -> viewModel.getTasksByTime()
                            "sort_by_priority" -> viewModel.sortTasksByPriority()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchTasksResponse.collect { apiResponse ->
                    when (apiResponse) {
                        is ApiResponse.Success -> {
                            adapter.submitList(apiResponse.data)
                            if (apiResponse.data?.isEmpty() == true) {
                                showNothingFoundMessage("Nothing found")
                            } else {
                                hideNothingFoundMessage()
                            }
                        }
                        is ApiResponse.Error -> {
                            Log.d("task", "SEARCH ERROR : ${apiResponse.error}")
                            showNothingFoundMessage("There was a network error")
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getTasksByTimeResponse.collect { apiResponse ->
                    when (apiResponse) {
                        is ApiResponse.Success -> {
                            adapter.submitList(apiResponse.data)
                            stopRefreshing()
                            hideProgressBar()
                            if (apiResponse.data?.isEmpty() == true) {
                                showNothingFoundMessage("Press + to add a task")
                            } else {
                                hideNothingFoundMessage()
                            }
                        }
                        is ApiResponse.Error -> {
                            Log.d("task", apiResponse.error.toString())
                            stopRefreshing()
                            hideProgressBar()
                            showErrorSnackbar()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getTasksByPriorityResponse.collect { apiResponse ->
                    when (apiResponse) {
                        is ApiResponse.Success -> {
                            adapter.submitList(apiResponse.data)
                            stopRefreshing()
                            hideProgressBar()
                            if (apiResponse.data?.isEmpty() == true) {
                                showNothingFoundMessage("Press + to add a Task")
                            } else {
                                hideNothingFoundMessage()
                            }
                        }
                        is ApiResponse.Error -> {
                            stopRefreshing()
                            hideProgressBar()
                            showErrorSnackbar()
                        }
                    }
                }
            }
        }

    }

    private fun showProgressBar() {
        Log.d("task", "Progress bar shown")
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun isRefreshing(): Boolean {
        return binding.swipeToRefresh.isRefreshing
    }

    private fun stopRefreshing() {
        binding.swipeToRefresh.isRefreshing = false
    }

    private fun showErrorSnackbar() {
        Snackbar.make(
            binding.coordinatorLayout,
            "There was a network error",
            LENGTH_LONG
        ).setAction("Ok") {}.show()
    }

    private fun showNothingFoundMessage(message: String) {
        binding.emptyMessageTextView.visibility = View.VISIBLE
        binding.emptyMessageTextView.text = message
    }

    private fun hideNothingFoundMessage() {
        binding.emptyMessageTextView.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)
        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as SearchView?
        searchView?.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort_by_date -> {
                lifecycleScope.launch {
                    SettingsDataStore.saveSortByToPreferences(
                        "sort_by_time",
                        requireContext()
                    )
                }
            }
            R.id.sort_by_priority -> {
                lifecycleScope.launch {
                    SettingsDataStore.saveSortByToPreferences(
                        "sort_by_priority",
                        requireContext()
                    )
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }



    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        query?.let {
            if (query == "") {
                viewModel.getTasksByTime()
            } else {
                viewModel.searchTasks(it)
            }
        }
        return true
    }
}
