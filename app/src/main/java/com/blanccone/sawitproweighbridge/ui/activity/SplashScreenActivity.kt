package com.blanccone.sawitproweighbridge.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import com.blanccone.core.ui.activity.CoreActivity
import com.blanccone.sawitproweighbridge.databinding.ActivitySplashScreenBinding

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : CoreActivity<ActivitySplashScreenBinding>() {

    override fun inflateLayout(inflater: LayoutInflater): ActivitySplashScreenBinding {
        return ActivitySplashScreenBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread {
            Thread.sleep(3000)
            runOnUiThread {
                HomeActivity.newInstance(this)
                finish()
            }
        }.start()
    }
}