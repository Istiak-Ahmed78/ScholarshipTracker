package com.istiak.scholarshiptracker

// Sealed class to represent different item types in RecyclerView
sealed class ScholarshipListItem {
    data class Header(val title: String, val count: Int) : ScholarshipListItem()
    data class ScholarshipItem(val scholarship: Scholarship) : ScholarshipListItem()
}
