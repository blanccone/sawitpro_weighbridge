import extensions.implementation
import extensions.kapt

plugins {
    id("commons.android-library")
}

android {
    namespace = BuildAndroidConfig.PERSISTENCE_ID
}

dependencies {
    implementation(project(BuildModules.CORE))
    implementation(Libs.ROOM)
    kapt(Libs.ROOM_COMPILER)
}