package com.ElbehiAhmed.studytimer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ElbehiAhmed.studytimer.databinding.ItemSessionBinding
import com.ElbehiAhmed.studytimer.entity.StudySession

// ListAdapter (instead of plain RecyclerView.Adapter) handles list diffing for us automatically
class SessionAdapter(
    private val onDeleteClick: (StudySession) -> Unit
) : ListAdapter<StudySession, SessionAdapter.SessionViewHolder>(SessionDiffCallback()) {

    // ViewHolder = holds references to one item's views (no repeated findViewById per scroll)
    inner class SessionViewHolder(private val binding: ItemSessionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(session: StudySession) {
            binding.tvDate.text = session.date
            binding.tvStartEnd.text = "${session.startTime} - ${session.endTime}"
            binding.tvDuration.text = "Duration: ${session.durationFormatted}"

            // Long-press to delete (simple gesture for now)
            binding.root.setOnLongClickListener {
                onDeleteClick(session)
                true // true = event consumed
            }
        }
    }

    // Called only when RecyclerView needs a BRAND NEW ViewHolder (not for every item, thanks to recycling)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ItemSessionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SessionViewHolder(binding)
    }

    // Called every time a ViewHolder (new or recycled) needs to display a specific item
    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

// DiffUtil compares old list vs new list to update ONLY changed items (not the whole list)
class SessionDiffCallback : DiffUtil.ItemCallback<StudySession>() {
    override fun areItemsTheSame(oldItem: StudySession, newItem: StudySession): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: StudySession, newItem: StudySession): Boolean {
        return oldItem == newItem // data class auto-generates equals() — compares all fields
    }
}