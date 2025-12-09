package com.istiak.scholarshiptracker

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class Scholarship(
    val id: String = System.currentTimeMillis().toString(),
    var name: String,
    var organization: String,
    var deadline: String,
    var fundAmount: String,
    var livingCosts: String,
    var requirements: String,
    var applicationLink: String,
    var notes: String,
    var status: String = "Not Applied" // Not Applied, Applied, Accepted, Rejected
) : Serializable {

    // Helper function to parse deadline string to Date
    fun getDeadlineDate(): Date? {
        if (deadline.isEmpty()) return null
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            format.parse(deadline)
        } catch (e: Exception) {
            null
        }
    }

    // Helper to check if deadline has passed
    fun isPast(): Boolean {
        val deadlineDate = getDeadlineDate() ?: return false
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        return deadlineDate.before(today.time)
    }

    // Helper to check if deadline is within next 30 days
    fun isNow(): Boolean {
        val deadlineDate = getDeadlineDate() ?: return false
        val today = Calendar.getInstance()
        val thirtyDaysLater = Calendar.getInstance()
        thirtyDaysLater.add(Calendar.DAY_OF_YEAR, 30)

        return !isPast() && (deadlineDate.before(thirtyDaysLater.time) || deadlineDate == thirtyDaysLater.time)
    }

    // Helper to check if deadline is more than 30 days away
    fun isUpcoming(): Boolean {
        val deadlineDate = getDeadlineDate() ?: return false
        return !isPast() && !isNow()
    }
}