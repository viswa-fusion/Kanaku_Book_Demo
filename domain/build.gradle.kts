plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
//    id ("kotlin-kapt")
//    id ("dagger.hilt.android.plugin")
}

android {
    namespace = "com.example.domain"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.room:room-common:2.6.1")

//    implementation(libs.dagger.android)
//    kapt(libs.dagger.compiler)
//    implementation (libs.google.dagger.android)
//    implementation (libs.dagger.android.support)
//    kapt (libs.dagger.android.processor)


//    implementation(libs.google.hilt.android)
//    implementation(libs.hilt.android.compiler)
//    implementation(libs.androidx.hilt.lifecycle.viewmodel)
//    implementation(libs.androidx.hilt.compiler)

}