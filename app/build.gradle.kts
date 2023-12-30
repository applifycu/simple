
plugins {
    id("com.android.application")
    
}

android {
    namespace = "com.arr.simple"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.arr.simple"
        minSdk = 23
        targetSdk = 34
        versionCode = 47
        versionName = "4.0.7-beta"
        
        vectorDrawables { 
            useSupportLibrary = true
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

        }
    }

    buildFeatures {
        viewBinding = true
        
    }
    
}


dependencies {

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.10")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.10")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
    implementation("androidx.navigation:navigation-ui:2.7.4")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.navigation:navigation-fragment:2.7.4")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.preference:preference:1.2.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.androidmads:QRGenerator:1.0.1")
    implementation("com.tbuonomo:dotsindicator:5.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")
    implementation("androidx.annotation:annotation:1.7.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0"){ isTransitive = false }
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.github.suitetecsa:suitetecsa-sdk-kotlin:0.1.9")
    implementation("com.airbnb.android:lottie:6.3.0")
    implementation("com.github.applifycu:bugsend:1.0.4-alpha3")
    implementation("com.github.applifycu:nautaclear:1.0.0-alpha2")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    
    /* local projects */
    implementation(project(":preference"))
    implementation(project(":fingerprint"))
    implementation(project(":ussd"))
    implementation(project(":simple-services"))
}
