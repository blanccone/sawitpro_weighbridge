package com.blanccone.sawitproweighbridge.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.blanccone.core.ui.activity.CoreActivity
import com.blanccone.core.ui.adapter.ViewPagerAdapter
import com.blanccone.sawitproweighbridge.databinding.ActivityListTicketBinding
import com.blanccone.sawitproweighbridge.ui.fragment.ListFirstWeightFragment
import com.blanccone.sawitproweighbridge.ui.fragment.ListSecondWeightFragment
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListTicketActivity: CoreActivity<ActivityListTicketBinding>() {

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
        setEvent()
    }

    private fun setViewPager() {
        val viewPagerAdapter = ViewPagerAdapter(this).apply {
            addFragment(ListFirstWeightFragment(), "First Weight")
            addFragment(ListSecondWeightFragment(), "Second Weight")
        }
        with(binding) {
            vpTicket.adapter = viewPagerAdapter

            TabLayoutMediator(tlTicket, vpTicket) { tab, position ->
                tab.text = viewPagerAdapter.getTitle(position)
            }.attach()
        }
    }

    private fun setEvent() {
        with(binding) {
            vpTicket.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    fabAddTicket.isVisible = position == 0
                }
            })

            fabAddTicket.setOnClickListener {
                EFormWeighmentActivity.newInstance(
                    this@ListTicketActivity,
                    EFormWeighmentActivity.FIRST_WEIGHT
                )
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        fun newInstance(context: Context) {
            val intent = Intent(context, ListTicketActivity::class.java)
            context.startActivity(intent)
        }
    }
}