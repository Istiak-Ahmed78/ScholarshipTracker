package com.istiak.scholarshiptracker

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ScholarshipManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("ScholarshipTrackerPrefs", Context.MODE_PRIVATE)
    
    private val gson = Gson()
    private val SCHOLARSHIPS_KEY = "scholarships"
    
    // Get all scholarships
    fun getAllScholarships(): MutableList<Scholarship> {
        val json = sharedPreferences.getString(SCHOLARSHIPS_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Scholarship>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }
    
    // Add new scholarship
    fun addScholarship(scholarship: Scholarship) {
        val scholarships = getAllScholarships()
        scholarships.add(scholarship)
        saveScholarships(scholarships)
    }
    
    // Update scholarship
    fun updateScholarship(updatedScholarship: Scholarship) {
        val scholarships = getAllScholarships()
        val index = scholarships.indexOfFirst { it.id == updatedScholarship.id }
        if (index != -1) {
            scholarships[index] = updatedScholarship
            saveScholarships(scholarships)
        }
    }
    
    // Delete scholarship
    fun deleteScholarship(scholarshipId: String) {
        val scholarships = getAllScholarships()
        scholarships.removeIf { it.id == scholarshipId }
        saveScholarships(scholarships)
    }
    
    // Get scholarship by ID
    fun getScholarshipById(id: String): Scholarship? {
        return getAllScholarships().find { it.id == id }
    }
    
    // Save scholarships to SharedPreferences
    private fun saveScholarships(scholarships: MutableList<Scholarship>) {
        val json = gson.toJson(scholarships)
        sharedPreferences.edit().putString(SCHOLARSHIPS_KEY, json).apply()
    }
    
    // Get scholarships by status
    fun getScholarshipsByStatus(status: String): List<Scholarship> {
        return getAllScholarships().filter { it.status == status }
    }
    
    // Clear all scholarships
    fun clearAll() {
        sharedPreferences.edit().remove(SCHOLARSHIPS_KEY).apply()
    }
}
