plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.istiak.scholarshiptracker"
    compileSdk = 34  // ✅ Fixed: Use simple number, not compileSdk{version=release(36)}

    defaultConfig {
        applicationId = "com.istiak.scholarshiptracker"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Core Android libraries
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // CardView for rounded cards
    implementation("androidx.cardview:cardview:1.0.0")

    // RecyclerView for lists
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Gson for JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")  // ✅ Fixed version
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}