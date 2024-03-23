package com.blanccone.sawitproweighbridge.ui.activity

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.blanccone.core.ui.activity.CoreActivity
import com.blanccone.sawitproweighbridge.databinding.ActivitySplashScreenBinding

class StartingActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { true }
        HomeActivity.newInstance(this)
        finish()
    }
}