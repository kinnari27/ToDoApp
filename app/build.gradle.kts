plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.todoapp"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.todoapp"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // AndroidX core
    implementation(libs.appcompat.v171)
    implementation(libs.core.ktx)
    implementation(libs.constraintlayout.v221)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    // Material Design components (Chips, FAB, TextInputLayout, MaterialCardView)
    implementation(libs.material.v1140)

    // RecyclerView
    implementation(libs.recyclerview)

    // ─── Room (local SQLite ORM) ───
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)   // generates DAO implementations

    // ─── Lifecycle (ViewModel + LiveData) ───
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // ─── Activity KTX (viewModels() delegate) ───
    implementation(libs.activity)

    // ─── Firebase & Google Auth ───
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}