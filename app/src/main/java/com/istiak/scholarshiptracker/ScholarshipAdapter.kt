package com.istiak.scholarshiptracker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.Date

class ScholarshipAdapter(
    private var scholarships: MutableList<Scholarship>,
    private val onItemClick: (Scholarship) -> Unit,
    private val onEditClick: (Scholarship) -> Unit,
    private val onDeleteClick: (Scholarship) -> Unit
) : RecyclerView.Adapter<ScholarshipAdapter.ScholarshipViewHolder>() {

    class ScholarshipViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardView)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvOrganization: TextView = view.findViewById(R.id.tvOrganization)
        val tvDeadline: TextView = view.findViewById(R.id.tvDeadline)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvDegree: TextView = view.findViewById(R.id.tvDegree)
        val tvLanguage: TextView = view.findViewById(R.id.tvLanguage)
        val tvReachType: TextView = view.findViewById(R.id.tvReachType)
        val tvFund: TextView = view.findViewById(R.id.tvFund)
        val tvDocuments: TextView = view.findViewById(R.id.tvDocuments)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val chipGroupDocuments: ChipGroup = view.findViewById(R.id.chipGroupDocuments)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        val layoutDeadlineWarning: LinearLayout = view.findViewById(R.id.layoutDeadlineWarning)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScholarshipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scholarship, parent, false)
        return ScholarshipViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScholarshipViewHolder, position: Int) {
        val scholarship = scholarships[position]

        holder.tvName.text = scholarship.name
        holder.tvOrganization.text = scholarship.organization

        // Display flexible deadline
        holder.tvDeadline.text = scholarship.getDisplayDeadline()

        // Display status with color
        holder.tvStatus.text = scholarship.status
        when (scholarship.status) {
            "Not Applied" -> holder.tvStatus.setBackgroundResource(R.drawable.badge_status_not_applied)
            "Applied" -> holder.tvStatus.setBackgroundResource(R.drawable.badge_status_applied)
            "Accepted" -> holder.tvStatus.setBackgroundResource(R.drawable.badge_status_accepted)
            "Rejected" -> holder.tvStatus.setBackgroundResource(R.drawable.badge_status_rejected)
        }

        // Display multiple degree types
        holder.tvDegree.text = scholarship.getDegreeTypesString()

        holder.tvLanguage.text = scholarship.languageRequirement
        holder.tvReachType.text = scholarship.applicationReachType

        // Display financial summary
        holder.tvFund.text = scholarship.getFinancialSummary()

        // Document progress
        val prepared = scholarship.getPreparedDocuments()
        val total = scholarship.documentsRequired.size
        val percentage = scholarship.getDocumentCompletionPercentage()

        holder.tvDocuments.text = "${prepared.size}/$total Documents ($percentage%)"
        holder.progressBar.progress = percentage

        // Update progress bar color based on completion
        when {
            percentage == 100 -> holder.progressBar.progressTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
            percentage >= 50 -> holder.progressBar.progressTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800"))
            else -> holder.progressBar.progressTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336"))
        }

        // Document chips
        holder.chipGroupDocuments.removeAllViews()
        prepared.take(3).forEach { docName ->
            val chip = Chip(holder.itemView.context)
            chip.text = docName
            chip.setChipBackgroundColorResource(android.R.color.holo_green_light)
            chip.setTextColor(Color.WHITE)
            chip.textSize = 10f
            holder.chipGroupDocuments.addView(chip)
        }

        if (prepared.size > 3) {
            val moreChip = Chip(holder.itemView.context)
            moreChip.text = "+${prepared.size - 3} more"
            moreChip.setChipBackgroundColorResource(android.R.color.darker_gray)
            moreChip.setTextColor(Color.WHITE)
            moreChip.textSize = 10f
            holder.chipGroupDocuments.addView(moreChip)
        }

        // Deadline warning (only for exact deadlines)
        if (scholarship.deadlineType == "Exact") {
            val deadlineDate = scholarship.getDeadlineDate()
            if (deadlineDate != null) {
                val today = Date()
                val daysUntil = ((deadlineDate.time - today.time) / (1000 * 60 * 60 * 24)).toInt()

                when {
                    daysUntil < 0 -> {
                        holder.layoutDeadlineWarning.visibility = View.VISIBLE
                        holder.layoutDeadlineWarning.setBackgroundColor(Color.parseColor("#757575"))
                        holder.layoutDeadlineWarning.findViewById<TextView>(R.id.tvDeadlineWarning).text = "Expired"
                    }
                    daysUntil in 0..7 -> {
                        holder.layoutDeadlineWarning.visibility = View.VISIBLE
                        holder.layoutDeadlineWarning.setBackgroundColor(Color.parseColor("#F44336"))
                        holder.layoutDeadlineWarning.findViewById<TextView>(R.id.tvDeadlineWarning).text =
                            "⚠️ $daysUntil days left"
                    }
                    daysUntil in 8..30 -> {
                        holder.layoutDeadlineWarning.visibility = View.VISIBLE
                        holder.layoutDeadlineWarning.setBackgroundColor(Color.parseColor("#FF9800"))
                        holder.layoutDeadlineWarning.findViewById<TextView>(R.id.tvDeadlineWarning).text =
                            "$daysUntil days left"
                    }
                    else -> {
                        holder.layoutDeadlineWarning.visibility = View.GONE
                    }
                }
            } else {
                holder.layoutDeadlineWarning.visibility = View.GONE
            }
        } else {
            // For non-exact deadlines, don't show warning
            holder.layoutDeadlineWarning.visibility = View.GONE
        }

        // Click listeners
        holder.cardView.setOnClickListener {
            onItemClick(scholarship)
        }

        holder.btnEdit.setOnClickListener {
            onEditClick(scholarship)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(scholarship)
        }
    }

    override fun getItemCount() = scholarships.size

    fun updateScholarships(newScholarships: List<Scholarship>) {
        scholarships.clear()
        scholarships.addAll(newScholarships)
        notifyDataSetChanged()
    }
}