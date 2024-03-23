package com.blanccone.sawitproweighbridge.ui.activity

import android.view.LayoutInflater
import com.blanccone.core.ui.activity.CoreActivity
import com.blanccone.sawitproweighbridge.databinding.ActivityEformWeighmentBinding

class EFormFirstWeightActivity : CoreActivity<ActivityEformWeighmentBinding>() {

    override fun inflateLayout(inflater: LayoutInflater): ActivityEformWeighmentBinding {
        return ActivityEformWeighmentBinding.inflate(inflater)
    }
}