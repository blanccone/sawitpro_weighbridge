import extensions.api

plugins {
    id("commons.android-library")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
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
    api(Libs.FIREBASE)

    kapt(Libs.GLIDE_COMPILER)
}