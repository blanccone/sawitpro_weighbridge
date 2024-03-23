plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    google()
    mavenCentral()
}

object PluginVersions {
    const val ANDROID_GRADLE = "8.1.1"
    const val KOTLIN = "1.8.10"
    const val DAGGER_HILT = "2.44"
}

dependencies {
    implementation("com.android.tools.build:gradle:${PluginVersions.ANDROID_GRADLE}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${PluginVersions.KOTLIN}")
    implementation("com.google.dagger:hilt-android-gradle-plugin:${PluginVersions.DAGGER_HILT}")
}