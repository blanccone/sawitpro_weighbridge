import extensions.api

plugins {
    id("commons.android-library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = BuildAndroidConfig.CORE_ID

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    api(Libs.CORE_KTX)
    api(Libs.LAYOUT)
    api(Libs.UI_LAYER)
    api(Libs.COROUTINES)
    api(Libs.GLIDE)
    api(Libs.PHOTO_LIBS)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    kapt(Libs.GLIDE_COMPILER)
}