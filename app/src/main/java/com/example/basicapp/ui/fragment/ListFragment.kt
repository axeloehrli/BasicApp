package com.example.basicapp.ui.fragment

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
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

class ListFragment : Fragment() {

    private val viewModel: TaskViewModel by activityViewModels {
        TaskViewModelFactory(
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

        SettingsDataStore = SettingsDataStore(requireContext())

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)
        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as SearchView?
        searchView?.isSubmitButtonEnabled = true
        searchView?.isIconified = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort_by_date -> {
                lifecycleScope.launch {
                    SettingsDataStore.saveSortByToPreferences(
                        "sort_by_date",
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

}
