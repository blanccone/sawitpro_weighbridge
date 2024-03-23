package com.blanccone.sawitproweighbridge.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.blanccone.core.ui.activity.CoreActivity
import com.blanccone.core.ui.adapter.ViewPagerAdapter
import com.blanccone.sawitproweighbridge.databinding.ActivityListTicketBinding
import com.blanccone.sawitproweighbridge.ui.fragment.ListFirstWeightFragment
import com.blanccone.sawitproweighbridge.ui.fragment.ListSecondWeightFragment
import com.google.android.material.tabs.TabLayoutMediator

class ListTicketActivity: CoreActivity<ActivityListTicketBinding>() {

    override fun inflateLayout(inflater: LayoutInflater): ActivityListTicketBinding {
        return ActivityListTicketBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setViewPager()
        setEvent()
    }

    private fun setViewPager() {
        val viewPagerAdapter = ViewPagerAdapter(this).apply {
            addFragment(ListFirstWeightFragment(), "First Weight")
            addFragment(ListSecondWeightFragment(), "Second Weight")
        }
        with(binding) {
            vpTicket.apply {
                adapter = viewPagerAdapter
                isUserInputEnabled = false
            }

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

            }
        }
    }

    companion object {

        fun newInstance(context: Context) {
            val intent = Intent(context, ListTicketActivity::class.java)
            context.startActivity(intent)
        }
    }
}