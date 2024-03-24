package com.blanccone.sawitproweighbridge.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import com.blanccone.core.ui.activity.CoreActivity
import com.blanccone.sawitproweighbridge.databinding.ActivityHomeBinding
import com.blanccone.sawitproweighbridge.ui.HomeMenuAdapter
import com.blanccone.sawitproweighbridge.util.Const

class HomeActivity: CoreActivity<ActivityHomeBinding>() {


    private val homeMenuAdapter by lazy { HomeMenuAdapter() }

    override fun inflateLayout(inflater: LayoutInflater): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setMenu()
        setEvent()
    }

    private fun setMenu() {
        binding.rvMenuUtama.adapter = homeMenuAdapter.apply {
            submitData(Const.homeMenu())
        }
    }

    private fun setEvent() {
        homeMenuAdapter.setOnItemClickListener {
            if (it == "tickets123") {
                ListTicketActivity.newInstance(this)
            } else {
                ListWeighmentResultActivity.newInstance(this)
            }
        }
    }

    companion object {

        fun newInstance(context: Context) {
            val intent = Intent(context, HomeActivity::class.java)
            context.startActivity(intent)
        }
    }
}