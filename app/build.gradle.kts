plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

   id("com.google.gms.google-services")

    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")

    //kotlin serialization plugin
    kotlin("plugin.serialization") version "2.0.21"
}



android {
    namespace = "s3154679.tees.ac.uk.recipiverse"
    compileSdk = 35

    defaultConfig {
        applicationId = "s3154679.tees.ac.uk.recipiverse"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

secrets {
    // Optionally specify a different file name containing your secrets.
    // The plugin defaults to "local.properties"
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be
    // checked in version control.
    defaultPropertiesFileName = "local.defaults.properties"
}

dependencies {

    implementation(libs.androidx.runtime.livedata)
    implementation(libs.firebase.auth.ktx)
    /**
     * Start of my dependencies
     **/

    // Jetpack Compose Navigation
    val nav_version = "2.8.3"
    implementation("androidx.navigation:navigation-compose:$nav_version")

    //Kotlin Serialization dependency
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))

    //google auth for signin/signup with email and password
    implementation("com.google.firebase:firebase-auth")

    // for signin/signup with google
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("androidx.credentials:credentials:1.5.0-alpha06")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    // optional - needed for credentials support from play services, for devices running
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0-alpha06")

    //for splash screen
    implementation("androidx.core:core-splashscreen:1.1.0-rc01")


    // For Firestore
    implementation("com.google.firebase:firebase-firestore")

    //For Storage
    implementation("com.google.firebase:firebase-storage")


    //for coil image library
    implementation("io.coil-kt.coil3:coil-compose:3.0.0-rc01")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.0-rc01")

    //permissions manager using accompanist
    implementation ("com.google.accompanist:accompanist-permissions:0.36.0")

    //Google Google Maps
    implementation ("com.google.maps.android:maps-compose:6.2.0")
    implementation ("com.google.maps.android:maps-compose-utils:6.2.0")
    implementation ("com.google.maps.android:maps-compose-widgets:6.2.0")
    //implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation ("com.google.android.libraries.places:places:4.0.0")


    // my depenpencies for exo player
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")

    /**
     * End of my dependencies
     **/


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(kotlin("script-runtime"))
}