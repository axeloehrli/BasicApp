package com.example.basicapp.ui.adapter

import android.location.Geocoder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.basicapp.data.model.*
import com.example.basicapp.databinding.ItemTaskBinding

class Adapter
    (private val onItemClicked: (Long) -> Unit) :
    ListAdapter<Task, Adapter.ViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem == newItem
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
                taskPriority.setCardBackgroundColor(task.getPriorityColor())
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
        holder.itemView.setOnClickListener {
            onItemClicked(task.id ?: return@setOnClickListener)
        }
        holder.bind(task)
    }
}



