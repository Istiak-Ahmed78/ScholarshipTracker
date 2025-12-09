# Scholarship Tracker - Android Application

A comprehensive Android application for managing scholarship applications, tracking deadlines, and organizing required documents. Built with Kotlin for students, researchers, and academic professionals.

## 📸 Screenshots

Here are screenshots from the actual application:

| Home Screen | Scholarship Details | Add/Edit Scholarship | Filter Popup |
|-------------|---------------------|----------------------|--------------|
| <img src="https://github.com/user-attachments/assets/fbc0be9c-5c1a-4251-aaae-023c3bdf0873" height="300"/> | <img src="https://github.com/user-attachments/assets/62df803f-6aef-4874-ad25-db6d3435a6c6" height="300"/> | <img src="https://github.com/user-attachments/assets/dd46e24d-040d-475f-8fa3-5ea4e98b24d8" height="300"/> | <img src="https://github.com/user-attachments/assets/55919fd8-6816-4078-8872-5ff4aac7d01b" height="300"/> |




## 🎥 Demo Video
<img src="https://github.com/user-attachments/assets/4e8d7f47-e3f7-4943-9c4e-bbe2130875c1" alt="Record gif" width="300">


## ✨ Features

### 🎓 Comprehensive Scholarship Management
✅ **Create, Edit, Delete** scholarship entries  
✅ **Track application status** (Not Applied, Applied, Accepted, Rejected)  
✅ **Organize by university/organization**  
✅ **Detailed scholarship information storage**

### ⏰ Smart Deadline Tracking
✅ **Multiple deadline types:** Exact Date, Month, Range, Rolling, TBA  
✅ **Visual deadline warnings** (7-day reminder banners)  
✅ **Auto-sorting** by deadline proximity  
✅ **Deadline notes** for additional information

### 📄 Document Management System
✅ **14 predefined document types:** SOP, LOR, CV, Transcripts, etc.  
✅ **Progress tracking** with percentage and visual progress bar  
✅ **Document readiness status** (prepared/not prepared)  
✅ **Filter by document completion** (All Ready, Some Missing, None Ready)

### 🔍 Advanced Filtering & Sorting
✅ **Real-time search** by name, organization, or requirements  
✅ **Multi-criteria filtering:**
   • Application Status  
   • Degree Type (Masters, PhD, Undergraduate, Postdoc)  
   • Language Requirement (IELTS, TOEFL, MOI, Duolingo, None)  
   • Application Reach (Institution, Professor, Both)  
✅ **Multiple sorting options:**
   • Deadline (earliest/latest first)  
   • Name (A-Z/Z-A)  
   • Application Status


### 🎓 Academic Details
✅ **Multiple degree support** (select multiple degree types)  
✅ **Language requirements** with test types  
✅ **Application requirements** field with rich text  
✅ **Application link** storage (clickable URLs)  
✅ **Notes section** for additional information

## 🏗️ Architecture

### Technology Stack
• **Language:** Kotlin
• **Minimum SDK:** Android 7.0 (API 24)  
• **Target SDK:** Android 14 (API 34)  
• **Architecture:** Single Activity with RecyclerView  
• **UI:** XML layouts with Material Design components  
• **Storage:** JSON-based local storage with Gson  
• **Build Tool:** Gradle

### Project Structure
```
scholarship-tracker/
├── app/
│   ├── src/main/
│   │   ├── java/com/istiak/scholarshiptracker/
│   │   │   ├── MainActivity.kt          # Main list screen
│   │   │   ├── ScholarshipDetailActivity.kt # Detailed view
│   │   │   ├── Scholarship.kt           # Data model
│   │   │   ├── ScholarshipManager.kt    # Data operations
│   │   │   └── ScholarshipAdapter.kt    # RecyclerView adapter
│   │   ├── res/
│   │   │   ├── layout/                  # All XML layouts
│   │   │   │   ├── activity_main.xml
│   │   │   │   ├── item_scholarship.xml
│   │   │   │   ├── dialog_add_edit_scholarship.xml
│   │   │   │   └── dialog_filter.xml
│   │   │   ├── drawable/               # Icons, shapes, gradients
│   │   │   ├── values/                 # Colors, strings, styles
│   │   │   └── mipmap/                 # App icons
│   │   └── AndroidManifest.xml
│   ├── build.gradle                    # Dependencies
│   └── proguard-rules.pro             # Release optimization
├── screenshots/                       # ADD YOUR SCREENSHOTS HERE
├── demo/                              # ADD YOUR DEMO VIDEO HERE
└── README.md                          # This file
```

## 📦 Installation

### Prerequisites
• Android Studio Flamingo (2022.2.1) or later  
• Android SDK 34  
• Java JDK 11 or higher

## 🚀 Usage Guide

### Adding a Scholarship
1. Tap the **+ (FAB)** button on main screen
2. Fill in basic information (Name, Organization, Status)
3. Set deadline type and date
4. Add financial details
5. Select degree types and language requirements
6. Use **"Auto-detect"** button to automatically identify required documents
7. Mark prepared documents as checked
8. Save to add to your list

### Managing Scholarships
• **Tap any scholarship** to view details  
• **Tap edit icon (✏️)** to modify information  
• **Tap delete icon (🗑️)** to remove scholarship  
• **Use search bar** to find specific scholarships  
• **Apply filters** to narrow down list  
• **Sort** by different criteria

### Document Management
• **Green progress bar** shows completion percentage  
• **Red chips** show incomplete documents  
• **Auto-detect** scans requirements text to suggest documents  
• **Filter by document readiness** to prioritize applications

## 📋 Document Types Supported
1. 📝 Statement of Purpose (SOP)
2. 📨 Letter of Recommendation (LOR)
3. 💌 Motivation Letter
4. 🔬 Research Proposal
5. 📋 Curriculum Vitae (CV)
6. 📊 Academic Transcripts
7. 🎓 Degree Certificate
8. 🌐 Language Test Scores
9. 🛂 Passport Copy
10. 📅 Study Plan
11. 📩 Reference Letters
12. 💼 Work Experience Proof
13. 🎨 Portfolio
14. 🏥 Medical Certificate

## 🔧 Technical Implementation

### Key Components
1. **ScholarshipManager** - Handles all CRUD operations
2. **ScholarshipAdapter** - Manages RecyclerView display
3. **Gson Integration** - JSON serialization/deserialization
4. **Material Design Components** - Modern UI elements
5. **ProGuard/R8** - Code optimization for release builds

### Performance Optimizations
• Efficient RecyclerView with ViewHolder pattern  
• Lazy loading of scholarship data  
• Optimized filtering algorithms  
• Minimized memory footprint  
• Release build optimizations enabled

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### ✅ Completed Features
• 📱 Core application functionality  
• 📄 Document management system  
• 🔍 Advanced filtering and sorting  
• 📦 Release APK generation  
• 🎨 Complete UI/UX design

APK link here:  [HERE](https://drive.google.com/file/d/1RrDcxFY3xTXpGNwWYeXCIgSBUqB6KMvD/view?usp=drive_link)


## 🙏 Acknowledgments

• Built with ❤️ using Kotlin and Android Studio  
• Material Design components for beautiful UI  
• Gson for efficient data serialization  
• All contributors and testers who helped improve the application

## Feel free to give suggestions
