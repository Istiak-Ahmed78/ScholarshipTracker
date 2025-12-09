package com.istiak.scholarshiptracker

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScholarshipAdapter
    private lateinit var scholarshipManager: ScholarshipManager
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var spinnerFilter: Spinner

    private var currentFilter = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scholarshipManager = ScholarshipManager(this)

        initializeViews()
        setupRecyclerView()
        setupFilterSpinner()
        loadScholarships()

        fabAdd.setOnClickListener {
            showAddEditDialog(null)
        }
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerView)
        fabAdd = findViewById(R.id.fabAdd)
        spinnerFilter = findViewById(R.id.spinnerFilter)
    }

    private fun setupRecyclerView() {
        adapter = ScholarshipAdapter(
            mutableListOf(),
            onItemClick = { scholarship -> showDetailsDialog(scholarship) },
            onEditClick = { scholarship -> showAddEditDialog(scholarship) },
            onDeleteClick = { scholarship -> confirmDelete(scholarship) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupFilterSpinner() {
        val statuses = arrayOf("All", "Not Applied", "Applied", "Accepted", "Rejected")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = spinnerAdapter

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                currentFilter = statuses[position]
                loadScholarships()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadScholarships() {
        val scholarships = if (currentFilter == "All") {
            scholarshipManager.getAllScholarships()
        } else {
            scholarshipManager.getScholarshipsByStatus(currentFilter).toMutableList()
        }

        // Sort by deadline (with null/empty deadlines at the end)
        val sortedScholarships = scholarships.sortedWith(compareBy<Scholarship> {
            it.getDeadlineDate() ?: Date(Long.MAX_VALUE)
        })

        // Group into sections
        val items = mutableListOf<ScholarshipListItem>()

        // Past deadlines section
        val pastScholarships = sortedScholarships.filter { it.isPast() }
        if (pastScholarships.isNotEmpty()) {
            items.add(ScholarshipListItem.Header("📅 Past Deadlines", pastScholarships.size))
            pastScholarships.forEach {
                items.add(ScholarshipListItem.ScholarshipItem(it))
            }
        }

        // Current/Now section (within 30 days)
        val nowScholarships = sortedScholarships.filter { it.isNow() }
        if (nowScholarships.isNotEmpty()) {
            items.add(ScholarshipListItem.Header("🔥 Active Now (Next 30 Days)", nowScholarships.size))
            nowScholarships.forEach {
                items.add(ScholarshipListItem.ScholarshipItem(it))
            }
        }

        // Upcoming section (more than 30 days away)
        val upcomingScholarships = sortedScholarships.filter { it.isUpcoming() }
        if (upcomingScholarships.isNotEmpty()) {
            items.add(ScholarshipListItem.Header("📆 Upcoming Applications", upcomingScholarships.size))
            upcomingScholarships.forEach {
                items.add(ScholarshipListItem.ScholarshipItem(it))
            }
        }

        // Scholarships without deadlines
        val noDeadline = sortedScholarships.filter { it.deadline.isEmpty() }
        if (noDeadline.isNotEmpty()) {
            items.add(ScholarshipListItem.Header("📝 No Deadline Set", noDeadline.size))
            noDeadline.forEach {
                items.add(ScholarshipListItem.ScholarshipItem(it))
            }
        }

        adapter.updateData(items)

        // Auto-scroll to "Now" section when app opens
        if (nowScholarships.isNotEmpty() && currentFilter == "All") {
            val nowIndex = items.indexOfFirst {
                it is ScholarshipListItem.Header && it.title.contains("Active Now")
            }
            if (nowIndex != -1) {
                recyclerView.scrollToPosition(nowIndex)
            }
        }
    }

    private fun showAddEditDialog(scholarship: Scholarship?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_scholarship, null)

        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etOrganization = dialogView.findViewById<EditText>(R.id.etOrganization)
        val etDeadline = dialogView.findViewById<EditText>(R.id.etDeadline)
        val etFund = dialogView.findViewById<EditText>(R.id.etFund)
        val etLivingCosts = dialogView.findViewById<EditText>(R.id.etLivingCosts)
        val etRequirements = dialogView.findViewById<EditText>(R.id.etRequirements)
        val etApplicationLink = dialogView.findViewById<EditText>(R.id.etApplicationLink)
        val etNotes = dialogView.findViewById<EditText>(R.id.etNotes)
        val spinnerStatus = dialogView.findViewById<Spinner>(R.id.spinnerStatus)

        // Setup status spinner
        val statuses = arrayOf("Not Applied", "Applied", "Accepted", "Rejected")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = statusAdapter

        // Date picker for deadline
        etDeadline.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    etDeadline.setText("$day/${month + 1}/$year")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Pre-fill if editing
        scholarship?.let {
            etName.setText(it.name)
            etOrganization.setText(it.organization)
            etDeadline.setText(it.deadline)
            etFund.setText(it.fundAmount)
            etLivingCosts.setText(it.livingCosts)
            etRequirements.setText(it.requirements)
            etApplicationLink.setText(it.applicationLink)
            etNotes.setText(it.notes)
            spinnerStatus.setSelection(statuses.indexOf(it.status))
        }

        AlertDialog.Builder(this)
            .setTitle(if (scholarship == null) "Add Scholarship" else "Edit Scholarship")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString()
                val organization = etOrganization.text.toString()
                val deadline = etDeadline.text.toString()
                val fund = etFund.text.toString()
                val livingCosts = etLivingCosts.text.toString()
                val requirements = etRequirements.text.toString()
                val applicationLink = etApplicationLink.text.toString()
                val notes = etNotes.text.toString()
                val status = spinnerStatus.selectedItem.toString()

                if (name.isEmpty() || organization.isEmpty()) {
                    Toast.makeText(this, "Name and Organization are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (scholarship == null) {
                    // Add new
                    val newScholarship = Scholarship(
                        name = name,
                        organization = organization,
                        deadline = deadline,
                        fundAmount = fund,
                        livingCosts = livingCosts,
                        requirements = requirements,
                        applicationLink = applicationLink,
                        notes = notes,
                        status = status
                    )
                    scholarshipManager.addScholarship(newScholarship)
                    Toast.makeText(this, "Scholarship added", Toast.LENGTH_SHORT).show()
                } else {
                    // Update existing
                    scholarship.name = name
                    scholarship.organization = organization
                    scholarship.deadline = deadline
                    scholarship.fundAmount = fund
                    scholarship.livingCosts = livingCosts
                    scholarship.requirements = requirements
                    scholarship.applicationLink = applicationLink
                    scholarship.notes = notes
                    scholarship.status = status

                    scholarshipManager.updateScholarship(scholarship)
                    Toast.makeText(this, "Scholarship updated", Toast.LENGTH_SHORT).show()
                }

                loadScholarships()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDetailsDialog(scholarship: Scholarship) {
        val daysUntil = getDaysUntilDeadline(scholarship)
        val urgencyText = when {
            daysUntil < 0 -> "⚠️ Deadline passed"
            daysUntil == 0 -> "🔥 Due TODAY!"
            daysUntil <= 7 -> "⚠️ Due in $daysUntil days"
            else -> ""
        }

        val message = """
            Organization: ${scholarship.organization}
            Deadline: ${scholarship.deadline} ${if (urgencyText.isNotEmpty()) "\n$urgencyText" else ""}
            Fund Amount: ${scholarship.fundAmount}
            Living Costs: ${scholarship.livingCosts}
            
            Requirements:
            ${scholarship.requirements}
            
            Application Link:
            ${scholarship.applicationLink}
            
            Notes:
            ${scholarship.notes}
            
            Status: ${scholarship.status}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle(scholarship.name)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setNeutralButton("Edit") { _, _ ->
                showAddEditDialog(scholarship)
            }
            .show()
    }

    private fun confirmDelete(scholarship: Scholarship) {
        AlertDialog.Builder(this)
            .setTitle("Delete Scholarship")
            .setMessage("Are you sure you want to delete ${scholarship.name}?")
            .setPositiveButton("Delete") { _, _ ->
                scholarshipManager.deleteScholarship(scholarship.id)
                Toast.makeText(this, "Scholarship deleted", Toast.LENGTH_SHORT).show()
                loadScholarships()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getDaysUntilDeadline(scholarship: Scholarship): Int {
        val deadlineDate = scholarship.getDeadlineDate() ?: return -999
        val today = Calendar.getInstance()
        val diffInMillis = deadlineDate.time - today.timeInMillis
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }
}