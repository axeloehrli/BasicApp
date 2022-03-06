package com.example.basicapp.ui.adapter

import android.graphics.Color
import android.location.Geocoder
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.basicapp.data.model.*
import com.example.basicapp.databinding.ItemTaskBinding

class Adapter
    (private val onItemClicked: (Int) -> Unit) :
    ListAdapter<Task, Adapter.ViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
                return when {
                    oldItem.id != newItem.id -> false
                    oldItem.title != newItem.title -> false
                    oldItem.description != newItem.description -> false
                    oldItem.time != newItem.time -> false
                    oldItem.latitude != newItem.latitude -> false
                    oldItem.longitude != newItem.longitude -> false
                    else -> true
                }
            }
        }
    }

    class ViewHolder(private var binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(task: Task) {
            binding.apply {
                taskTitle.text = task.title
                taskDescription.text = task.description
                taskDayOfWeek.text = task.getFormattedDayOfWeek()
                taskDayOfMonth.text = task.getFormattedDayOfMonth()
                taskMonth.text = task.getFormattedMonth()
                taskTime.text = task.getFormattedTime()
                taskLocation.text = task.getFormattedLocation(Geocoder(taskLayout.context))

                executePendingBindings()
            }
        }
    }

    //creates the layout for each list item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    // binds data to each list item
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = getItem(holder.adapterPosition)
        Log.d("task", "BOUND")
        holder.itemView.setOnClickListener {
            onItemClicked(task.id)
        }
        holder.bind(task)


    }
}



