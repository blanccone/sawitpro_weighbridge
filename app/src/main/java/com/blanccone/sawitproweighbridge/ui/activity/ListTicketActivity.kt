package com.blanccone.sawitproweighbridge.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.blanccone.core.ui.activity.CoreActivity
import com.blanccone.core.ui.adapter.ViewPagerAdapter
import com.blanccone.sawitproweighbridge.databinding.ActivityListTicketBinding
import com.blanccone.sawitproweighbridge.ui.activity.EFormWeighmentActivity.Companion.FIRST_WEIGHT
import com.blanccone.sawitproweighbridge.ui.activity.EFormWeighmentActivity.Companion.SECOND_WEIGHT
import com.blanccone.sawitproweighbridge.ui.fragment.ListWeighmentProcessFragment
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListTicketActivity : CoreActivity<ActivityListTicketBinding>() {

    override fun inflateLayout(inflater: LayoutInflater): ActivityListTicketBinding {
        return ActivityListTicketBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Tickets"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        setViewPager()
        onBackPressedEvent()
    }

    private fun setViewPager() {
        val viewPagerAdapter = ViewPagerAdapter(this).apply {
            addFragment(ListWeighmentProcessFragment.newInstance(FIRST_WEIGHT), "First Weight")
            addFragment(ListWeighmentProcessFragment.newInstance(SECOND_WEIGHT), "Second Weight")
        }
        with(binding) {
            vpTicket.adapter = viewPagerAdapter

            TabLayoutMediator(tlTicket, vpTicket) { tab, position ->
                tab.text = viewPagerAdapter.getTitle(position)
            }.attach()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onBackPressedEvent() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                setResult(Activity.RESULT_OK)
                finish()
            }
        })
    }

    companion object {

        fun resultInstance(context: Context): Intent {
            return Intent(context, ListTicketActivity::class.java)
        }
    }
}