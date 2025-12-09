package com.istiak.scholarshiptracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScholarshipAdapter(
    private var items: MutableList<ScholarshipListItem>,
    private val onItemClick: (Scholarship) -> Unit,
    private val onEditClick: (Scholarship) -> Unit,
    private val onDeleteClick: (Scholarship) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_SCHOLARSHIP = 1
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSectionTitle: TextView = view.findViewById(R.id.tvSectionTitle)
        val tvCount: TextView = view.findViewById(R.id.tvCount)
    }

    class ScholarshipViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvScholarshipName)
        val tvOrganization: TextView = view.findViewById(R.id.tvOrganization)
        val tvDeadline: TextView = view.findViewById(R.id.tvDeadline)
        val tvFund: TextView = view.findViewById(R.id.tvFund)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ScholarshipListItem.Header -> VIEW_TYPE_HEADER
            is ScholarshipListItem.ScholarshipItem -> VIEW_TYPE_SCHOLARSHIP
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_section_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_scholarship, parent, false)
                ScholarshipViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ScholarshipListItem.Header -> {
                val headerHolder = holder as HeaderViewHolder
                headerHolder.tvSectionTitle.text = item.title
                headerHolder.tvCount.text = "${item.count}"
            }
            is ScholarshipListItem.ScholarshipItem -> {
                val scholarshipHolder = holder as ScholarshipViewHolder
                val scholarship = item.scholarship

                scholarshipHolder.tvName.text = scholarship.name
                scholarshipHolder.tvOrganization.text = scholarship.organization
                scholarshipHolder.tvDeadline.text = if (scholarship.deadline.isEmpty()) {
                    "No deadline set"
                } else {
                    "Deadline: ${scholarship.deadline}"
                }
                scholarshipHolder.tvFund.text = "Fund: ${scholarship.fundAmount}"
                scholarshipHolder.tvStatus.text = scholarship.status

                // Set status color
                when (scholarship.status) {
                    "Not Applied" -> scholarshipHolder.tvStatus.setTextColor(
                        scholarshipHolder.itemView.context.getColor(android.R.color.holo_orange_dark)
                    )
                    "Applied" -> scholarshipHolder.tvStatus.setTextColor(
                        scholarshipHolder.itemView.context.getColor(android.R.color.holo_blue_dark)
                    )
                    "Accepted" -> scholarshipHolder.tvStatus.setTextColor(
                        scholarshipHolder.itemView.context.getColor(android.R.color.holo_green_dark)
                    )
                    "Rejected" -> scholarshipHolder.tvStatus.setTextColor(
                        scholarshipHolder.itemView.context.getColor(android.R.color.holo_red_dark)
                    )
                }

                // Highlight urgent deadlines (within 7 days)
                if (scholarship.deadline.isNotEmpty()) {
                    val daysUntil = getDaysUntilDeadline(scholarship)
                    if (daysUntil in 0..7) {
                        scholarshipHolder.tvDeadline.setTextColor(
                            scholarshipHolder.itemView.context.getColor(android.R.color.holo_red_dark)
                        )
                    } else {
                        scholarshipHolder.tvDeadline.setTextColor(
                            scholarshipHolder.itemView.context.getColor(android.R.color.black)
                        )
                    }
                }

                scholarshipHolder.itemView.setOnClickListener { onItemClick(scholarship) }
                scholarshipHolder.btnEdit.setOnClickListener { onEditClick(scholarship) }
                scholarshipHolder.btnDelete.setOnClickListener { onDeleteClick(scholarship) }
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: MutableList<ScholarshipListItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun getDaysUntilDeadline(scholarship: Scholarship): Int {
        val deadlineDate = scholarship.getDeadlineDate() ?: return -1
        val today = java.util.Calendar.getInstance()
        val diffInMillis = deadlineDate.time - today.timeInMillis
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }
}