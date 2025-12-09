package com.istiak.scholarshiptracker

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class Scholarship(
    val id: String = System.currentTimeMillis().toString(),
    var name: String,
    var organization: String,

    // FLEXIBLE DEADLINE SYSTEM
    var deadlineType: String = "Exact", // Exact, Month, Range, Rolling, TBA
    var deadline: String = "", // For exact dates: DD/MM/YYYY
    var deadlineMonth: String = "", // For month-based: "January 2026"
    var deadlineRange: String = "", // For ranges: "October-March" or "Dec 2025 - Mar 2026"
    var deadlineNotes: String = "", // Additional info: "Varies by program", "Opens in January"

    // ENHANCED FINANCIAL INFO (replacing single fundAmount)
    var tuitionCovered: Boolean = false, // Full tuition waiver
    var monthlyStipend: String = "", // e.g., "€1,000-€1,400/month"
    var airfareCovered: Boolean = false,
    var accommodationCovered: Boolean = false,
    var healthInsuranceCovered: Boolean = false,
    var otherBenefits: String = "", // Visa fees, travel allowance, etc.

    var livingCosts: String = "", // Keep for backward compatibility
    var requirements: String,
    var applicationLink: String,
    var notes: String,
    var status: String = "Not Applied", // Not Applied, Applied, Accepted, Rejected

    // ENHANCED DEGREE TYPE (multiple selection)
    var degreeTypes: MutableList<String> = mutableListOf("Masters"), // Masters, PhD, Undergraduate, Postdoc
    var degreeType: String = "Masters", // For backward compatibility

    var languageRequirement: String = "IELTS", // IELTS, TOEFL, MOI, Duolingo, None
    var applicationReachType: String = "Institution", // Institution, Professor, Both

    // ENHANCED DOCUMENT TRACKING
    var documentsRequired: MutableMap<String, Boolean> = mutableMapOf(
        "SOP" to false,
        "LOR" to false,
        "Motivation Letter" to false,
        "Research Proposal" to false,
        "CV" to false,
        "Transcripts" to false,
        "Degree Certificate" to false,
        "Language Test" to false,
        "Passport Copy" to false,
        "Study Plan" to false,
        "Reference Letters" to false,
        "Work Experience Proof" to false,
        "Portfolio" to false,
        "Medical Certificate" to false
    )
) : Serializable {

    // Helper function to get display deadline
    fun getDisplayDeadline(): String {
        return when (deadlineType) {
            "Exact" -> if (deadline.isNotEmpty()) deadline else "No deadline set"
            "Month" -> if (deadlineMonth.isNotEmpty()) deadlineMonth else "No deadline set"
            "Range" -> if (deadlineRange.isNotEmpty()) deadlineRange else "No deadline set"
            "Rolling" -> "Rolling admission"
            "TBA" -> "To be announced"
            else -> "No deadline set"
        }
    }

    // Helper to get financial summary
    fun getFinancialSummary(): String {
        val benefits = mutableListOf<String>()
        if (tuitionCovered) benefits.add("Tuition")
        if (monthlyStipend.isNotEmpty()) benefits.add("Stipend")
        if (airfareCovered) benefits.add("Airfare")
        if (accommodationCovered) benefits.add("Accommodation")
        if (healthInsuranceCovered) benefits.add("Insurance")

        return if (benefits.isEmpty()) {
            "Check details"
        } else {
            benefits.joinToString(", ")
        }
    }

    // Helper function to parse deadline string to Date (for exact dates only)
    fun getDeadlineDate(): Date? {
        if (deadlineType != "Exact" || deadline.isEmpty()) return null
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            format.parse(deadline)
        } catch (e: Exception) {
            null
        }
    }

    // Helper to check if deadline has passed (only for exact dates)
    fun isPast(): Boolean {
        if (deadlineType != "Exact") return false
        val deadlineDate = getDeadlineDate() ?: return false
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        return deadlineDate.before(today.time)
    }

    // Helper to check if deadline is within next 30 days (only for exact dates)
    fun isNow(): Boolean {
        if (deadlineType != "Exact") return false
        val deadlineDate = getDeadlineDate() ?: return false
        val today = Calendar.getInstance()
        val thirtyDaysLater = Calendar.getInstance()
        thirtyDaysLater.add(Calendar.DAY_OF_YEAR, 30)

        return !isPast() && (deadlineDate.before(thirtyDaysLater.time) || deadlineDate == thirtyDaysLater.time)
    }

    // Helper to check if deadline is more than 30 days away (only for exact dates)
    fun isUpcoming(): Boolean {
        if (deadlineType != "Exact") return false
        val deadlineDate = getDeadlineDate() ?: return false
        return !isPast() && !isNow()
    }

    // For non-exact deadlines, they go to "Upcoming" by default unless TBA
    fun getSectionCategory(): String {
        return when (deadlineType) {
            "Exact" -> when {
                isPast() -> "Past"
                isNow() -> "Now"
                isUpcoming() -> "Upcoming"
                else -> "NoDeadline"
            }
            "TBA", "" -> "NoDeadline"
            else -> "Upcoming" // Month, Range, Rolling go to Upcoming
        }
    }

    // Get list of documents that are NOT yet prepared
    fun getMissingDocuments(): List<String> {
        return documentsRequired.filter { !it.value && it.key.isNotEmpty() }.keys.toList()
    }

    // Get list of documents that ARE prepared
    fun getPreparedDocuments(): List<String> {
        return documentsRequired.filter { it.value && it.key.isNotEmpty() }.keys.toList()
    }

    // Check if all required documents are prepared
    fun allDocumentsPrepared(): Boolean {
        return documentsRequired.all { it.value || it.key.isEmpty() }
    }

    // Get percentage of documents prepared
    fun getDocumentCompletionPercentage(): Int {
        val total = documentsRequired.size
        if (total == 0) return 100
        val prepared = documentsRequired.count { it.value }
        return (prepared * 100) / total
    }

    // ENHANCED Auto-detect required documents from requirements text
    fun autoDetectDocuments(requirementsText: String) {
        val lowerReq = requirementsText.toLowerCase()

        // Reset all to false first
        documentsRequired.keys.forEach { documentsRequired[it] = false }

        // Detect SOP / Statement of Purpose
        if (lowerReq.contains("sop") || lowerReq.contains("statement of purpose") ||
            lowerReq.contains("personal statement")) {
            documentsRequired["SOP"] = false
        }

        // Detect LOR / Letters of Recommendation
        if (lowerReq.contains("lor") || lowerReq.contains("letter of recommendation") ||
            lowerReq.contains("reference letter") || lowerReq.contains("recommendation letter") ||
            lowerReq.contains("2 lor") || lowerReq.contains("3 lor") || lowerReq.contains("two references")) {
            documentsRequired["LOR"] = false
        }

        // Detect Motivation Letter / Cover Letter
        if (lowerReq.contains("motivation") || lowerReq.contains("cover letter") ||
            lowerReq.contains("motivation letter")) {
            documentsRequired["Motivation Letter"] = false
        }

        // Detect Research Proposal / Research Plan
        if (lowerReq.contains("research proposal") || lowerReq.contains("research plan") ||
            lowerReq.contains("research statement")) {
            documentsRequired["Research Proposal"] = false
        }

        // Detect CV / Resume
        if (lowerReq.contains("cv") || lowerReq.contains("resume") ||
            lowerReq.contains("curriculum vitae")) {
            documentsRequired["CV"] = false
        }

        // Detect Transcripts / Academic Records
        if (lowerReq.contains("transcript") || lowerReq.contains("academic record") ||
            lowerReq.contains("grade sheet") || lowerReq.contains("mark sheet")) {
            documentsRequired["Transcripts"] = false
        }

        // Detect Degree Certificate / Bachelor's Certificate
        if (lowerReq.contains("degree certificate") || lowerReq.contains("bachelor") ||
            lowerReq.contains("graduation certificate") || lowerReq.contains("diploma")) {
            documentsRequired["Degree Certificate"] = false
        }

        // Detect Language Test (IELTS/TOEFL/etc)
        if (lowerReq.contains("ielts") || lowerReq.contains("toefl") ||
            lowerReq.contains("language test") || lowerReq.contains("english proficiency") ||
            lowerReq.contains("pte") || lowerReq.contains("duolingo")) {
            documentsRequired["Language Test"] = false
        }

        // Detect Passport
        if (lowerReq.contains("passport") || lowerReq.contains("id proof") ||
            lowerReq.contains("identification")) {
            documentsRequired["Passport Copy"] = false
        }

        // Detect Study Plan
        if (lowerReq.contains("study plan") || lowerReq.contains("academic plan") ||
            lowerReq.contains("course plan")) {
            documentsRequired["Study Plan"] = false
        }

        // Detect Reference Letters (separate from LOR)
        if (lowerReq.contains("reference") || lowerReq.contains("referee") ||
            lowerReq.contains("academic reference")) {
            documentsRequired["Reference Letters"] = false
        }

        // Detect Work Experience
        if (lowerReq.contains("work experience") || lowerReq.contains("employment") ||
            lowerReq.contains("professional experience") || lowerReq.contains("work history")) {
            documentsRequired["Work Experience Proof"] = false
        }

        // Detect Portfolio (for creative/design programs)
        if (lowerReq.contains("portfolio") || lowerReq.contains("work samples") ||
            lowerReq.contains("creative work")) {
            documentsRequired["Portfolio"] = false
        }

        // Detect Medical Certificate / Health Insurance
        if (lowerReq.contains("medical") || lowerReq.contains("health certificate") ||
            lowerReq.contains("health insurance") || lowerReq.contains("medical examination")) {
            documentsRequired["Medical Certificate"] = false
        }
    }

    // Get degree types as comma-separated string
    fun getDegreeTypesString(): String {
        return degreeTypes.joinToString(", ")
    }
}