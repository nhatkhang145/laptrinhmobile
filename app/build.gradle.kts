plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.apporderfood"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.apporderfood"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.cardview)
    implementation(libs.coordinatorlayout)
    implementation(libs.iconics.core)
    implementation(libs.iconics.views)
    implementation(libs.fontawesome.typeface)
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    // Retrofit - goi HTTP API den Spring Boot backend
    implementation(libs.retrofit.core)
    // Gson converter - tu dong chuyen JSON <-> Object Java
    implementation(libs.retrofit.gson)
    implementation(libs.gsonfull)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}