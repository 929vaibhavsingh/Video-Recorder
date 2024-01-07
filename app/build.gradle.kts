plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.jr.liveclipper"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jr.liveclipper"
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
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(Dep.coreKtx)
    implementation(Dep.appCompat)
    implementation(Dep.materialDesign)
    implementation(Dep.cameraCore)
    implementation(Dep.camera2)
    implementation(Dep.cameraVideo)
    implementation(Dep.cameraLifecycle)
    implementation(Dep.cameraView)
    implementation(Dep.androidxLifecycleKtx)
    implementation(Dep.timber)
    implementation(Dep.concurrentFuture)
    implementation(Dep.composeUI)
    implementation(Dep.composeTooling)
    implementation(Dep.composePermission)
    implementation(Dep.composeMaterial)
    implementation(Dep.composeActivity)
    implementation(Dep.composeNavigation)
    implementation(Dep.composeNavigationAnimation)
    implementation(Dep.coil)

    testImplementation(Dep.junit)
    androidTestImplementation(Dep.androidJUnit)
    androidTestImplementation(Dep.expresso)

    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    implementation("com.arthenica:mobile-ffmpeg-full-gpl:4.4.LTS")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.0-beta03")


//    testImplementation("junit:junit:4.13.2")
//    androidTestImplementation("androidx.test.ext:junit:1.1.5")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
//    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
//    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
//    debugImplementation("androidx.compose.ui:ui-tooling")
//    debugImplementation("androidx.compose.ui:ui-test-manifest")
}