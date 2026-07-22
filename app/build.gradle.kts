plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.firebase.perf)
}

android {
    namespace = "com.worldclock.app_themes"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cw.worldclock.alram.clock.timer.widgets"
        minSdk = 26
        targetSdk = 36
        versionCode = 9
        versionName = "1.0.9"
        setProperty("archivesBaseName", "World_Clock-v$versionCode($versionName)")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

//    flavorDimensions.add("version")
//    productFlavors {
//        create("production") {
//            dimension = "version"
//            applicationId = "com.cw.worldclock.alram.clock.timer.widgets"
//        }
//        create("dev") {
//            dimension = "version"
//            applicationId = "com.test.clock"
//        }
//    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "admobSDKKey", "ca-app-pub-9304966727485663~4278170280")
            resValue("string", "facebook_app_id", "1938017986712136")
        }

        debug {
            isMinifyEnabled = false
            versionNameSuffix = "-debug"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "admobSDKKey", "ca-app-pub-3940256099942544~3347511713")
            resValue("string", "facebook_app_id", "123456789012345")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    bundle {
        language {
            enableSplit = false
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.config)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.locale.helper)
    implementation(libs.sdp)
    implementation(libs.ssp)
    implementation(libs.billing)
    implementation(libs.glide)
    ksp(libs.glide.compiler)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
    implementation(libs.circular.seek.bar)
    implementation(libs.play.services.ads)
    implementation(libs.facebook.audience.network)
    implementation(libs.lifecycle.process)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.shimmer)
    implementation(libs.lottie)
    implementation(libs.timber)
    implementation(libs.ump)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.firebase.messaging)
    implementation(libs.play.app.update)
    implementation(libs.play.app.update.ktx)
    implementation(libs.firebase.perf)
    implementation("com.hbb20:ccp:2.5.0")
}