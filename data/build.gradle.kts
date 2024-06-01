plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
//    id ("dagger.hilt.android.plugin")
}

android {
    namespace = "com.example.data"
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
    implementation(project(":domain"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    val room_version = "2.6.1"

    api ("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    // To use Kotlin annotation processing tool (kapt)
    kapt("androidx.room:room-compiler:$room_version")



    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$room_version")
//    implementation(libs.dagger.android)
//    kapt(libs.dagger.compiler)
//    implementation (libs.google.dagger.android)
//    implementation (libs.dagger.android.support)
//    kapt (libs.dagger.android.processor)


//    //com.google.dagger:hilt-android:2.44
//    implementation(libs.google.hilt.android)
//    //com.google.dagger:hilt-android-compiler:2.44
//    implementation(libs.hilt.android.compiler)
//    //androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03
//    implementation(libs.androidx.hilt.lifecycle.viewmodel)
//    //androidx.hilt:hilt-compiler:1.2.0
//    implementation(libs.androidx.hilt.compiler)
}