package com.istiak.scholarshiptracker

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*
import android.os.Looper
import android.os.Handler
class MainActivity : AppCompatActivity() {

    private lateinit var rvScholarships: RecyclerView
    private lateinit var scholarshipAdapter: ScholarshipAdapter
    private lateinit var scholarshipManager: ScholarshipManager
    private lateinit var etSearch: EditText
    private lateinit var btnFilter: ImageButton
    private lateinit var btnSort: ImageButton
    private lateinit var chipGroupActiveFilters: ChipGroup

    // Filter state
    private var activeFilters = mutableMapOf<String, MutableSet<String>>(
        "status" to mutableSetOf(),
        "degree" to mutableSetOf(),
        "language" to mutableSetOf(),
        "reach" to mutableSetOf(),
        "documents" to mutableSetOf()
    )
    private var currentSortOption = "deadline_asc"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scholarshipManager = ScholarshipManager(this)

        // Initialize views
        rvScholarships = findViewById(R.id.rvScholarships)
        etSearch = findViewById(R.id.etSearch)
        btnFilter = findViewById(R.id.btnFilter)
        btnSort = findViewById(R.id.btnSort)
        chipGroupActiveFilters = findViewById(R.id.chipGroupActiveFilters)
        val fabAdd: com.google.android.material.floatingactionbutton.FloatingActionButton =
            findViewById(R.id.fabAdd)

        // Setup RecyclerView
        scholarshipAdapter = ScholarshipAdapter(
            scholarships = mutableListOf(),
            onItemClick = { scholarship -> openDetailActivity(scholarship) },
            onEditClick = { scholarship -> showAddEditDialog(scholarship) },
            onDeleteClick = { scholarship -> deleteScholarship(scholarship) }
        )
        rvScholarships.layoutManager = LinearLayoutManager(this)
        rvScholarships.adapter = scholarshipAdapter

        // Setup search
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterAndSortScholarships()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Setup buttons
        fabAdd.setOnClickListener { showAddEditDialog() }
        btnFilter.setOnClickListener { showFilterDialog() }
        btnSort.setOnClickListener { showSortDialog() }

        loadScholarships()
        checkEditRequest()
    }
    private fun checkEditRequest() {
        val scholarshipId = intent.getStringExtra("EDIT_SCHOLARSHIP_ID")
        if (scholarshipId != null) {
            // Find the scholarship and open edit dialog
            val scholarship = scholarshipManager.getScholarshipById(scholarshipId)
            if (scholarship != null) {
                // Small delay to ensure activity is fully loaded
                Handler(Looper.getMainLooper()).postDelayed({
                    showAddEditDialog(scholarship)
                }, 300)
            }
        }
    }
    private fun loadScholarships() {
        filterAndSortScholarships()
    }

    private fun filterAndSortScholarships() {
        val searchQuery = etSearch.text.toString().trim()
        val allScholarships = scholarshipManager.getAllScholarships()

        // Apply search filter
        var filtered = if (searchQuery.isEmpty()) {
            allScholarships
        } else {
            allScholarships.filter { scholarship ->
                scholarship.name.contains(searchQuery, ignoreCase = true) ||
                        scholarship.organization.contains(searchQuery, ignoreCase = true) ||
                        scholarship.requirements.contains(searchQuery, ignoreCase = true)
            }
        }

        // Apply advanced filters
        if (activeFilters["status"]?.isNotEmpty() == true) {
            filtered = filtered.filter { it.status in activeFilters["status"]!! }
        }
        if (activeFilters["degree"]?.isNotEmpty() == true) {
            filtered = filtered.filter { scholarship ->
                activeFilters["degree"]!!.any { degree -> degree in scholarship.degreeTypes }
            }
        }
        if (activeFilters["language"]?.isNotEmpty() == true) {
            filtered = filtered.filter { it.languageRequirement in activeFilters["language"]!! }
        }
        if (activeFilters["reach"]?.isNotEmpty() == true) {
            filtered = filtered.filter { it.applicationReachType in activeFilters["reach"]!! }
        }
        if (activeFilters["documents"]?.isNotEmpty() == true) {
            filtered = filtered.filter { scholarship ->
                when (activeFilters["documents"]!!.first()) {
                    "All Ready" -> scholarship.allDocumentsPrepared()
                    "Some Missing" -> !scholarship.allDocumentsPrepared() && scholarship.getPreparedDocuments().isNotEmpty()
                    "None Ready" -> scholarship.getPreparedDocuments().isEmpty()
                    else -> true
                }
            }
        }

        // Apply sorting
        val sorted = when (currentSortOption) {
            "deadline_asc" -> filtered.sortedWith(compareBy(nullsLast()) { it.getDeadlineDate() })
            "deadline_desc" -> filtered.sortedWith(compareByDescending(nullsFirst()) { it.getDeadlineDate() })
            "name_asc" -> filtered.sortedBy { it.name.lowercase() }
            "name_desc" -> filtered.sortedByDescending { it.name.lowercase() }
            "status" -> filtered.sortedBy {
                when (it.status) {
                    "Not Applied" -> 0
                    "Applied" -> 1
                    "Accepted" -> 2
                    "Rejected" -> 3
                    else -> 4
                }
            }
            else -> filtered
        }

        scholarshipAdapter.updateScholarships(sorted)
        updateActiveFilterChips()
    }

    private fun updateActiveFilterChips() {
        chipGroupActiveFilters.removeAllViews()

        val hasFilters = activeFilters.values.any { it.isNotEmpty() }
        chipGroupActiveFilters.visibility = if (hasFilters) View.VISIBLE else View.GONE

        activeFilters.forEach { (category, values) ->
            values.forEach { value ->
                val chip = Chip(this)
                chip.text = value
                chip.isCloseIconVisible = true
                chip.setChipBackgroundColorResource(android.R.color.holo_blue_light)
                chip.setTextColor(Color.WHITE)
                chip.setOnCloseIconClickListener {
                    activeFilters[category]?.remove(value)
                    filterAndSortScholarships()
                }
                chipGroupActiveFilters.addView(chip)
            }
        }

        if (hasFilters) {
            val clearAllChip = Chip(this)
            clearAllChip.text = "Clear All"
            clearAllChip.setChipBackgroundColorResource(android.R.color.holo_red_light)
            clearAllChip.setTextColor(Color.WHITE)
            clearAllChip.setOnClickListener {
                activeFilters.values.forEach { it.clear() }
                filterAndSortScholarships()
            }
            chipGroupActiveFilters.addView(clearAllChip)
        }
    }

    private fun showAddEditDialog(scholarship: Scholarship? = null) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_edit_scholarship)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        // Get all views
        val etName = dialog.findViewById<TextInputEditText>(R.id.etName)
        val etOrganization = dialog.findViewById<TextInputEditText>(R.id.etOrganization)
        val spinnerStatus = dialog.findViewById<Spinner>(R.id.spinnerStatus)

        // Deadline fields
        val spinnerDeadlineType = dialog.findViewById<Spinner>(R.id.spinnerDeadlineType)
        val etDeadline = dialog.findViewById<TextInputEditText>(R.id.etDeadline)
        val etDeadlineMonth = dialog.findViewById<TextInputEditText>(R.id.etDeadlineMonth)
        val etDeadlineRange = dialog.findViewById<TextInputEditText>(R.id.etDeadlineRange)
        val etDeadlineNotes = dialog.findViewById<TextInputEditText>(R.id.etDeadlineNotes)
        val layoutExactDate = dialog.findViewById<LinearLayout>(R.id.layoutExactDate)
        val layoutMonthDeadline = dialog.findViewById<LinearLayout>(R.id.layoutMonthDeadline)
        val layoutRangeDeadline = dialog.findViewById<LinearLayout>(R.id.layoutRangeDeadline)

        // Financial fields
        val cbTuitionCovered = dialog.findViewById<CheckBox>(R.id.cbTuitionCovered)
        val etMonthlyStipend = dialog.findViewById<TextInputEditText>(R.id.etMonthlyStipend)
        val cbAirfareCovered = dialog.findViewById<CheckBox>(R.id.cbAirfareCovered)
        val cbAccommodationCovered = dialog.findViewById<CheckBox>(R.id.cbAccommodationCovered)
        val cbHealthInsurance = dialog.findViewById<CheckBox>(R.id.cbHealthInsurance)
        val etOtherBenefits = dialog.findViewById<TextInputEditText>(R.id.etOtherBenefits)
        val etLivingCosts = dialog.findViewById<TextInputEditText>(R.id.etLivingCosts)

        // Degree type checkboxes
        val cbDegreeMasters = dialog.findViewById<CheckBox>(R.id.cbDegreeMasters)
        val cbDegreePhD = dialog.findViewById<CheckBox>(R.id.cbDegreePhD)
        val cbDegreeUndergraduate = dialog.findViewById<CheckBox>(R.id.cbDegreeUndergraduate)
        val cbDegreePostdoc = dialog.findViewById<CheckBox>(R.id.cbDegreePostdoc)

        val spinnerLanguage = dialog.findViewById<Spinner>(R.id.spinnerLanguage)
        val spinnerReachType = dialog.findViewById<Spinner>(R.id.spinnerReachType)
        val etRequirements = dialog.findViewById<TextInputEditText>(R.id.etRequirements)
        val btnAutoDetect = dialog.findViewById<Button>(R.id.btnAutoDetect)

        // Document checkboxes (14 total)
        val cbDocSOP = dialog.findViewById<CheckBox>(R.id.cbDocSOP)
        val cbDocLOR = dialog.findViewById<CheckBox>(R.id.cbDocLOR)
        val cbDocMotivation = dialog.findViewById<CheckBox>(R.id.cbDocMotivation)
        val cbDocResearch = dialog.findViewById<CheckBox>(R.id.cbDocResearch)
        val cbDocCV = dialog.findViewById<CheckBox>(R.id.cbDocCV)
        val cbDocTranscripts = dialog.findViewById<CheckBox>(R.id.cbDocTranscripts)
        val cbDocDegree = dialog.findViewById<CheckBox>(R.id.cbDocDegree)
        val cbDocLanguage = dialog.findViewById<CheckBox>(R.id.cbDocLanguage)
        val cbDocPassport = dialog.findViewById<CheckBox>(R.id.cbDocPassport)
        val cbDocStudyPlan = dialog.findViewById<CheckBox>(R.id.cbDocStudyPlan)
        val cbDocReferences = dialog.findViewById<CheckBox>(R.id.cbDocReferences)
        val cbDocWorkExp = dialog.findViewById<CheckBox>(R.id.cbDocWorkExp)
        val cbDocPortfolio = dialog.findViewById<CheckBox>(R.id.cbDocPortfolio)
        val cbDocMedical = dialog.findViewById<CheckBox>(R.id.cbDocMedical)

        val etApplicationLink = dialog.findViewById<TextInputEditText>(R.id.etApplicationLink)
        val etNotes = dialog.findViewById<TextInputEditText>(R.id.etNotes)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialog.findViewById<Button>(R.id.btnSave)

        // Setup spinners
        val statusOptions = arrayOf("Not Applied", "Applied", "Accepted", "Rejected")
        spinnerStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)

        val deadlineTypes = arrayOf("Exact", "Month", "Range", "Rolling", "TBA")
        spinnerDeadlineType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, deadlineTypes)

        val languageOptions = arrayOf("IELTS", "TOEFL", "MOI", "Duolingo", "None")
        spinnerLanguage.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languageOptions)

        val reachOptions = arrayOf("Institution", "Professor", "Both")
        spinnerReachType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, reachOptions)

        // Handle deadline type changes
        spinnerDeadlineType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                layoutExactDate.visibility = if (position == 0) View.VISIBLE else View.GONE
                layoutMonthDeadline.visibility = if (position == 1) View.VISIBLE else View.GONE
                layoutRangeDeadline.visibility = if (position == 2) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Date picker for exact deadline
        etDeadline.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    etDeadline.setText(String.format("%02d/%02d/%d", day, month + 1, year))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // If editing, populate fields
        scholarship?.let { s ->
            etName.setText(s.name)
            etOrganization.setText(s.organization)
            spinnerStatus.setSelection(statusOptions.indexOf(s.status))

            // Deadline
            val deadlineTypePosition = deadlineTypes.indexOf(s.deadlineType)
            spinnerDeadlineType.setSelection(if (deadlineTypePosition >= 0) deadlineTypePosition else 0)
            etDeadline.setText(s.deadline)
            etDeadlineMonth.setText(s.deadlineMonth)
            etDeadlineRange.setText(s.deadlineRange)
            etDeadlineNotes.setText(s.deadlineNotes)

            // Financial
            cbTuitionCovered.isChecked = s.tuitionCovered
            etMonthlyStipend.setText(s.monthlyStipend)
            cbAirfareCovered.isChecked = s.airfareCovered
            cbAccommodationCovered.isChecked = s.accommodationCovered
            cbHealthInsurance.isChecked = s.healthInsuranceCovered
            etOtherBenefits.setText(s.otherBenefits)
            etLivingCosts.setText(s.livingCosts)

            // Degree types
            cbDegreeMasters.isChecked = "Masters" in s.degreeTypes
            cbDegreePhD.isChecked = "PhD" in s.degreeTypes
            cbDegreeUndergraduate.isChecked = "Undergraduate" in s.degreeTypes
            cbDegreePostdoc.isChecked = "Postdoc" in s.degreeTypes

            spinnerLanguage.setSelection(languageOptions.indexOf(s.languageRequirement))
            spinnerReachType.setSelection(reachOptions.indexOf(s.applicationReachType))
            etRequirements.setText(s.requirements)

            // Documents
            cbDocSOP.isChecked = s.documentsRequired["SOP"] ?: false
            cbDocLOR.isChecked = s.documentsRequired["LOR"] ?: false
            cbDocMotivation.isChecked = s.documentsRequired["Motivation Letter"] ?: false
            cbDocResearch.isChecked = s.documentsRequired["Research Proposal"] ?: false
            cbDocCV.isChecked = s.documentsRequired["CV"] ?: false
            cbDocTranscripts.isChecked = s.documentsRequired["Transcripts"] ?: false
            cbDocDegree.isChecked = s.documentsRequired["Degree Certificate"] ?: false
            cbDocLanguage.isChecked = s.documentsRequired["Language Test"] ?: false
            cbDocPassport.isChecked = s.documentsRequired["Passport Copy"] ?: false
            cbDocStudyPlan.isChecked = s.documentsRequired["Study Plan"] ?: false
            cbDocReferences.isChecked = s.documentsRequired["Reference Letters"] ?: false
            cbDocWorkExp.isChecked = s.documentsRequired["Work Experience Proof"] ?: false
            cbDocPortfolio.isChecked = s.documentsRequired["Portfolio"] ?: false
            cbDocMedical.isChecked = s.documentsRequired["Medical Certificate"] ?: false

            etApplicationLink.setText(s.applicationLink)
            etNotes.setText(s.notes)
        }

        // Auto-detect button
        btnAutoDetect.setOnClickListener {
            val reqText = etRequirements.text.toString()
            if (reqText.isNotEmpty()) {
                val tempScholarship = Scholarship(
                    id = "",
                    name = "",
                    organization = "",
                    requirements = "",
                    applicationLink = "",  // Add this
                    notes = ""             // Add this
                )
                tempScholarship.autoDetectDocuments(reqText)

                // Update checkboxes - set to CHECKED if detected (documents are initially needed, not prepared)
                cbDocSOP.isChecked = tempScholarship.documentsRequired["SOP"] == false
                cbDocLOR.isChecked = tempScholarship.documentsRequired["LOR"] == false
                cbDocMotivation.isChecked = tempScholarship.documentsRequired["Motivation Letter"] == false
                cbDocResearch.isChecked = tempScholarship.documentsRequired["Research Proposal"] == false
                cbDocCV.isChecked = tempScholarship.documentsRequired["CV"] == false
                cbDocTranscripts.isChecked = tempScholarship.documentsRequired["Transcripts"] == false
                cbDocDegree.isChecked = tempScholarship.documentsRequired["Degree Certificate"] == false
                cbDocLanguage.isChecked = tempScholarship.documentsRequired["Language Test"] == false
                cbDocPassport.isChecked = tempScholarship.documentsRequired["Passport Copy"] == false
                cbDocStudyPlan.isChecked = tempScholarship.documentsRequired["Study Plan"] == false
                cbDocReferences.isChecked = tempScholarship.documentsRequired["Reference Letters"] == false
                cbDocWorkExp.isChecked = tempScholarship.documentsRequired["Work Experience Proof"] == false
                cbDocPortfolio.isChecked = tempScholarship.documentsRequired["Portfolio"] == false
                cbDocMedical.isChecked = tempScholarship.documentsRequired["Medical Certificate"] == false

                Toast.makeText(this, "Documents detected! Uncheck ones you've prepared.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter requirements first", Toast.LENGTH_SHORT).show()
            }
        }

        // Save button
        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val org = etOrganization.text.toString().trim()

            if (name.isEmpty() || org.isEmpty()) {
                Toast.makeText(this, "Name and Organization are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create or update scholarship
            val newScholarship = scholarship?.copy() ?: Scholarship(
                id = System.currentTimeMillis().toString(),
                name = "",
                organization = "",
                requirements = "",
                applicationLink = "",  // Add this
                notes = ""             // Add this
            )

            newScholarship.name = name
            newScholarship.organization = org
            newScholarship.status = spinnerStatus.selectedItem.toString()

            // Deadline
            newScholarship.deadlineType = spinnerDeadlineType.selectedItem.toString()
            newScholarship.deadline = etDeadline.text.toString()
            newScholarship.deadlineMonth = etDeadlineMonth.text.toString()
            newScholarship.deadlineRange = etDeadlineRange.text.toString()
            newScholarship.deadlineNotes = etDeadlineNotes.text.toString()

            // Financial
            newScholarship.tuitionCovered = cbTuitionCovered.isChecked
            newScholarship.monthlyStipend = etMonthlyStipend.text.toString()
            newScholarship.airfareCovered = cbAirfareCovered.isChecked
            newScholarship.accommodationCovered = cbAccommodationCovered.isChecked
            newScholarship.healthInsuranceCovered = cbHealthInsurance.isChecked
            newScholarship.otherBenefits = etOtherBenefits.text.toString()
            newScholarship.livingCosts = etLivingCosts.text.toString()

            // Degree types
            newScholarship.degreeTypes.clear()
            if (cbDegreeMasters.isChecked) newScholarship.degreeTypes.add("Masters")
            if (cbDegreePhD.isChecked) newScholarship.degreeTypes.add("PhD")
            if (cbDegreeUndergraduate.isChecked) newScholarship.degreeTypes.add("Undergraduate")
            if (cbDegreePostdoc.isChecked) newScholarship.degreeTypes.add("Postdoc")

            // Ensure at least one degree type
            if (newScholarship.degreeTypes.isEmpty()) {
                newScholarship.degreeTypes.add("Masters")
            }

            // Set primary degree type for backward compatibility
            newScholarship.degreeType = newScholarship.degreeTypes.first()

            newScholarship.languageRequirement = spinnerLanguage.selectedItem.toString()
            newScholarship.applicationReachType = spinnerReachType.selectedItem.toString()
            newScholarship.requirements = etRequirements.text.toString()

            // Documents (checked = prepared)
            newScholarship.documentsRequired["SOP"] = cbDocSOP.isChecked
            newScholarship.documentsRequired["LOR"] = cbDocLOR.isChecked
            newScholarship.documentsRequired["Motivation Letter"] = cbDocMotivation.isChecked
            newScholarship.documentsRequired["Research Proposal"] = cbDocResearch.isChecked
            newScholarship.documentsRequired["CV"] = cbDocCV.isChecked
            newScholarship.documentsRequired["Transcripts"] = cbDocTranscripts.isChecked
            newScholarship.documentsRequired["Degree Certificate"] = cbDocDegree.isChecked
            newScholarship.documentsRequired["Language Test"] = cbDocLanguage.isChecked
            newScholarship.documentsRequired["Passport Copy"] = cbDocPassport.isChecked
            newScholarship.documentsRequired["Study Plan"] = cbDocStudyPlan.isChecked
            newScholarship.documentsRequired["Reference Letters"] = cbDocReferences.isChecked
            newScholarship.documentsRequired["Work Experience Proof"] = cbDocWorkExp.isChecked
            newScholarship.documentsRequired["Portfolio"] = cbDocPortfolio.isChecked
            newScholarship.documentsRequired["Medical Certificate"] = cbDocMedical.isChecked

            newScholarship.applicationLink = etApplicationLink.text.toString()
            newScholarship.notes = etNotes.text.toString()

            // Save
            if (scholarship == null) {
                scholarshipManager.addScholarship(newScholarship)
                Toast.makeText(this, "Scholarship added", Toast.LENGTH_SHORT).show()
            } else {
                scholarshipManager.updateScholarship(newScholarship)
                Toast.makeText(this, "Scholarship updated", Toast.LENGTH_SHORT).show()
            }

            loadScholarships()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showFilterDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_filter)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        // Get filter views
        val cgStatus = dialog.findViewById<ChipGroup>(R.id.cgFilterStatus)
        val cgDegree = dialog.findViewById<ChipGroup>(R.id.cgFilterDegree)
        val cgLanguage = dialog.findViewById<ChipGroup>(R.id.cgFilterLanguage)
        val cgReach = dialog.findViewById<ChipGroup>(R.id.cgFilterReach)
        val cgDocuments = dialog.findViewById<ChipGroup>(R.id.cgFilterDocuments)
        val btnApply = dialog.findViewById<Button>(R.id.btnApplyFilters)
        val btnClear = dialog.findViewById<Button>(R.id.btnClearFilters)

        // Populate and check existing filters
        populateFilterChips(cgStatus, arrayOf("Not Applied", "Applied", "Accepted", "Rejected"), "status")
        populateFilterChips(cgDegree, arrayOf("Masters", "PhD", "Undergraduate", "Postdoc"), "degree")
        populateFilterChips(cgLanguage, arrayOf("IELTS", "TOEFL", "MOI", "Duolingo", "None"), "language")
        populateFilterChips(cgReach, arrayOf("Institution", "Professor", "Both"), "reach")
        populateFilterChips(cgDocuments, arrayOf("All Ready", "Some Missing", "None Ready"), "documents")

        btnApply.setOnClickListener {
            filterAndSortScholarships()
            dialog.dismiss()
        }

        btnClear.setOnClickListener {
            activeFilters.values.forEach { it.clear() }
            filterAndSortScholarships()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun populateFilterChips(chipGroup: ChipGroup, options: Array<String>, category: String) {
        chipGroup.removeAllViews()
        options.forEach { option ->
            val chip = Chip(this)
            chip.text = option
            chip.isCheckable = true
            chip.isChecked = activeFilters[category]?.contains(option) == true
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    activeFilters[category]?.add(option)
                } else {
                    activeFilters[category]?.remove(option)
                }
            }
            chipGroup.addView(chip)
        }
    }

    private fun showSortDialog() {
        val options = arrayOf(
            "Deadline (Earliest First)",
            "Deadline (Latest First)",
            "Name (A-Z)",
            "Name (Z-A)",
            "Status"
        )

        val optionKeys = arrayOf(
            "deadline_asc",
            "deadline_desc",
            "name_asc",
            "name_desc",
            "status"
        )

        val currentIndex = optionKeys.indexOf(currentSortOption)

        android.app.AlertDialog.Builder(this)
            .setTitle("Sort By")
            .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                currentSortOption = optionKeys[which]
                filterAndSortScholarships()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteScholarship(scholarship: Scholarship) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Delete Scholarship")
            .setMessage("Are you sure you want to delete '${scholarship.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                scholarshipManager.deleteScholarship(scholarship.id)
                loadScholarships()
                Toast.makeText(this, "Scholarship deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openDetailActivity(scholarship: Scholarship) {
        val intent = Intent(this, ScholarshipDetailActivity::class.java)
        intent.putExtra("SCHOLARSHIP_ID", scholarship.id)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadScholarships()
    }
}