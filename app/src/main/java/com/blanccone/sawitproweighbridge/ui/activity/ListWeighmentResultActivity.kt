package com.blanccone.sawitproweighbridge.ui.activity

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import com.blanccone.core.ui.activity.CoreActivity
import com.blanccone.sawitproweighbridge.databinding.ActivityListWeighmentResultBinding

class ListWeighmentResultActivity: CoreActivity<ActivityListWeighmentResultBinding>() {

    override fun inflateLayout(inflater: LayoutInflater): ActivityListWeighmentResultBinding {
        return ActivityListWeighmentResultBinding.inflate(inflater)
    }

    companion object {

        fun newInstance(context: Context) {
            val intent = Intent(context, ListWeighmentResultActivity::class.java)
            context.startActivity(intent)
        }
    }
}