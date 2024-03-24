pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://mvnrepository.com/artifact")
        maven(url = "https://maven.google.com")
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "SawitPro Weighbridge"
include(":app")
include(":core")
include(":persistence")
