package extensions

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler


/**
 * Adds a dependency to the `implementation` configuration.
 *
 * @param dependencyNotation name of dependency to add at specific configuration
 *
 * @return the dependency
 */
fun DependencyHandler.implementation(dependencyNotation: String): Dependency? =
    add("implementation", dependencyNotation)

/**
 * Adds a dependency to the `debugImplementation` configuration.
 *
 * @param dependencyNotation name of dependency to add at specific configuration
 *
 * @return the dependency
 */
fun DependencyHandler.debugImplementation(dependencyNotation: String): Dependency? =
    add("debugImplementation", dependencyNotation)

/**
 * Adds a dependency to the `releaseImplementation` configuration.
 *
 * @param dependencyNotation name of dependency to add at specific configuration
 *
 * @return the dependency
 */
fun DependencyHandler.releaseImplementation(dependencyNotation: String): Dependency? =
    add("releaseImplementation", dependencyNotation)

/**
 * Adds a dependency to the `api` configuration.
 *
 * @param dependencyNotation name of dependency to add at specific configuration
 *
 * @return the dependency
 */
fun DependencyHandler.api(dependencyNotation: String): Dependency? =
    add("api", dependencyNotation)

/**
 * Adds a dependency to the `debugImplementation` configuration.
 *
 * @param dependencyNotation name of dependency to add at specific configuration
 *
 * @return the dependency
 */
fun DependencyHandler.debugApi(dependencyNotation: String): Dependency? =
    add("debugApi", dependencyNotation)

/**
 * Adds a dependency to the `releaseImplementation` configuration.
 *
 * @param dependencyNotation name of dependency to add at specific configuration
 *
 * @return the dependency
 */
fun DependencyHandler.releaseApi(dependencyNotation: String): Dependency? =
    add("releaseApi", dependencyNotation)

/**
 * Adds a dependency to the `kapt` configuration.
 *
 * @param dependencyNotation name of dependency to add at specific configuration
 *
 * @return the dependency
 */
fun DependencyHandler.kapt(dependencyNotation: String): Dependency? =
    add("kapt", dependencyNotation)

/**
 * Adds a dependency to the `testImplementation` configuration.
 *
 * @param dependencyNotation name of dependency to add at specific configuration
 *
 * @return the dependency
 */
fun DependencyHandler.testImplementation(dependencyNotation: String): Dependency? =
    add("testImplementation", dependencyNotation)


/**
 * Adds a dependency to the `androidTestImplementation` configuration.
 *
 * @param dependencyNotation name of dependency to add at specific configuration
 *
 * @return the dependency
 */
fun DependencyHandler.androidTestImplementation(dependencyNotation: String): Dependency? =
    add("androidTestImplementation", dependencyNotation)

/**
 * Adds a dependency to the `annotationProcessor` configuration.
 *
 * @param annotationProcessor name of dependency to add at specific configuration
 *
 * @return the dependency
 */
fun DependencyHandler.annotationProcessor(dependencyNotation: String): Dependency? =
    add("annotationProcessor", dependencyNotation)

fun DependencyHandler.api(dependencyNotation: List<Any>) {
    for (dep in dependencyNotation) {
        add("api", dep)
    }
}

fun DependencyHandler.implementation(dependencyNotation: List<Any>) {
    for (dep in dependencyNotation) {
        add("implementation", dep)
    }
}

fun DependencyHandler.debugImplementation(dependencyNotation: List<Any>) {
    for (dep in dependencyNotation) {
        add("debugImplementation", dep)
    }
}

fun DependencyHandler.androidTestImplementation(dependencyNotation: List<Any>) {
    for (dep in dependencyNotation) {
        add("androidTestImplementation", dep)
    }
}

fun DependencyHandler.testImplementation(dependencyNotation: List<Any>) {
    for (dep in dependencyNotation) {
        add("testImplementation", dep)
    }
}
