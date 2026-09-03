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
            binding.tvSensors.text = listOfNotNull(
                session.temperature?.let { "Temperature: ${formatValue(it)}°C" },
                session.humidity?.let { "Humidity: ${formatValue(it)}%" },
                session.brightness?.let { "Brightness: ${formatValue(it)}%" }
            ).ifEmpty { listOf("Sensor readings unavailable") }.joinToString("  |  ")

            // Long-press to delete (simple gesture for now)
            binding.root.setOnLongClickListener {
                onDeleteClick(session)
                true // true = event consumed
            }
        }

        private fun formatValue(value: Double): String =
            if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(value)
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