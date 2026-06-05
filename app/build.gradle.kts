plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.worldclock.app_themes"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cw.worldclock.alram.clock.timer.widgets"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "1.0.3"
        setProperty("archivesBaseName", "World_Clock-v$versionCode($versionName)")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions.add("version")
    productFlavors {
        create("production") {
            dimension = "version"
            applicationId = "com.cw.worldclock.alram.clock.timer.widgets"
        }
        create("dev") {
            dimension = "version"
            applicationId = "com.test.clock"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "admobSDKKey", "ca-app-pub-9304966727485663~4278170280")
        }

        debug {
            isMinifyEnabled = false
            versionNameSuffix = "-debug"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "admobSDKKey", "ca-app-pub-3940256099942544~3347511713")
            resValue("string", "admob_banner_id", "ca-app-pub-3940256099942544/9214589741")
            resValue("string", "admob_native_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_interstitial_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admobRewardAd", "ca-app-pub-3940256099942544/5224354917")
            resValue("string", "admobRewardAdsInitialID", "ca-app-pub-3940256099942544/5354046379")
            resValue("string", "admob_openAd_id", "ca-app-pub-3940256099942544/9257395921")
            resValue("string", "admobOnboardingNativeAd", "ca-app-pub-3940256099942544/2247696110")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
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
    implementation ("com.zeugmasolutions.localehelper:locale-helper-android:1.5.1")
    implementation ("com.intuit.sdp:sdp-android:1.0.6")
    implementation ("com.intuit.ssp:ssp-android:1.0.6")
    implementation ("com.android.billingclient:billing:7.1.1")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    kapt ("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("androidx.room:room-runtime:2.6.1")
    kapt( "androidx.room:room-compiler:2.6.1")
    implementation( "androidx.room:room-ktx:2.6.1")
    implementation ("me.tankery.lib:circularSeekBar:1.4.1")
    implementation ("com.google.android.gms:play-services-ads:24.5.0")  //Admob
//    implementation(project(":Roozi"))
    implementation("androidx.lifecycle:lifecycle-process:2.9.2")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

}