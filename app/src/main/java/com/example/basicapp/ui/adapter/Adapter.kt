package com.example.basicapp.ui.adapter

import android.location.Geocoder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.basicapp.data.model.*
import com.example.basicapp.databinding.ItemBinding

class Adapter
    (private val onItemClicked: (Task) -> Unit) :
    ListAdapter<Task, Adapter.ViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
                return when {
                    oldItem.id != newItem.id -> false
                    oldItem.status != newItem.status -> false
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

    class ViewHolder(private var binding: ItemBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(task: Task) {
            binding.apply {
                itemTitle.text = task.title
                itemDescription.text = task.description
                itemDayOfWeek.text = task.getFormattedDayOfWeek()
                itemDayOfMonth.text = task.getFormattedDayOfMonth()
                itemMonth.text = task.getFormattedMonth()
                itemTime.text = task.getFormattedTime()
                itemLocation.text = task.getFormattedLocation(Geocoder(itemLayout.context))
                itemStatus.text = task.status

                executePendingBindings()
            }
        }
    }

    //creates the layout for each list item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    // binds data to each list item
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = getItem(holder.adapterPosition)

        holder.itemView.setOnClickListener {
            onItemClicked(task)
        }
        holder.bind(task)


    }
}



