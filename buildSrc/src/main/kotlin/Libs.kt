object Libs {
    const val CORE_KTX = "androidx.core:core-ktx:${Versions.CORE_KTX}"

    val COROUTINES = listOf(
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.COROUTINES}",
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.COROUTINES}"
    )

    const val DAGGER_HILT = "com.google.dagger:hilt-android:${Versions.DAGGER_HILT}"
    const val DAGGER_HILT_COMPILER = "com.google.dagger:hilt-android-compiler:${Versions.DAGGER_HILT}"

    val RETROFIT = listOf(
        "com.squareup.retrofit2:retrofit:${Versions.RETROFIT}",
        "com.squareup.retrofit2:converter-gson:${Versions.RETROFIT}",
        "com.squareup.okhttp3:logging-interceptor:${Versions.OKHTPP}"
    )

    const val CHUCKER_DEBUG = "com.github.chuckerteam.chucker:library:${Versions.CHUCKER}"
    const val CHUCKER_RELEASE = "com.github.chuckerteam.chucker:library-no-op:${Versions.CHUCKER}"

    const val PAGING = "androidx.paging:paging-runtime-ktx:${Versions.PAGING}"

    val ROOM = listOf(
        "androidx.room:room-ktx:${Versions.ROOM}",
        "androidx.room:room-runtime:${Versions.ROOM}"
    )
    const val ROOM_COMPILER = "androidx.room:room-compiler:${Versions.ROOM}"

    val LIFECYCLE = listOf(
        "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.LIFECYCLE}",
        "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.LIFECYCLE}",
        "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.LIFECYCLE}",
        "com.github.hadilq.liveevent:liveevent:${Versions.LIVEEVENT}"
    )

    val UI_LAYER = listOf(
        "androidx.activity:activity-ktx:${Versions.ACTIVITY_KTX}",
        "androidx.fragment:fragment-ktx:${Versions.FRAGMENT_KTX}"
    )

    val LAYOUT = listOf(
        "androidx.appcompat:appcompat:${Versions.APPCOMPAT}",
        "com.google.android.material:material:${Versions.MATERIAL}",
        "androidx.constraintlayout:constraintlayout:${Versions.CONSTRAINT_LAYOUT}",
        "androidx.recyclerview:recyclerview:${Versions.RECYCLER_VIEW}",
        "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.SWIPE_REFRESH_LAYOUT}",
        "androidx.core:core-splashscreen:${Versions.SPLASH_SCREEN}"
    )

    const val GLIDE = "com.github.bumptech.glide:glide:${Versions.GLIDE}"
    const val GLIDE_COMPILER = "com.github.bumptech.glide:compiler:${Versions.GLIDE}"

    val PHOTO_LIBS = listOf(
        "com.github.chrisbanes:PhotoView:${Versions.PHOTO_VIEW}",
        "com.chensl.rotatephotoview:rotatephotoview:${Versions.ROTATE_PHOTO}"
    )
}