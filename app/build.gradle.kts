plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
//    id("kotlin-kapt")
    kotlin("plugin.serialization") version "1.9.20"
    alias(libs.plugins.kotlin.safeargs)
}


android {
    namespace = "pt.cravodeabril.movies"
    compileSdk = 36

    defaultConfig {
        applicationId = "pt.cravodeabril.movies"
        minSdk = 26
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
    kotlinOptions {

        jvmTarget = "11"
    }



    buildFeatures.viewBinding = true
}

kotlin {
    sourceSets.all {
        languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
        languageSettings.optIn("kotlin.io.path.ExperimentalPathApi")
        languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
        languageSettings.optIn("kotlin.time.ExperimentalTime")
    }
}

dependencies {

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.coil3.coil)
    implementation(libs.coil.network.okhttp)


    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.annotation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.datetime)
}