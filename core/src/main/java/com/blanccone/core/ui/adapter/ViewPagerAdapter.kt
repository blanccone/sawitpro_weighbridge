package com.blanccone.core.ui.adapter

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val fragmentList: MutableList<Fragment> = mutableListOf()
    private val fragmentTitle: MutableList<String> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun addFragment(fragment: Fragment, title: String, index: Int? = null) {
        if (isFragmentExist(fragment)) return
        if (index != null) {
            fragmentList.add(index, fragment)
            fragmentTitle.add(index, title)
        } else {
            fragmentList.add(fragment)
            fragmentTitle.add(title)
        }
        notifyDataSetChanged()

    }

    fun isFragmentExist(fragment: Fragment) : Boolean {
        return fragmentList.contains(fragment)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeFragment(fragment: Fragment, title: String) {
        fragmentList.remove(fragment)
        fragmentTitle.remove(title)
        notifyDataSetChanged()
    }

    fun getFragment(position: Int) : Fragment {
        return fragmentList[position]
    }

    override fun containsItem(itemId: Long): Boolean {
        val pagesId = fragmentList.map { it.hashCode().toLong() }
        return pagesId.contains(itemId)
    }

    override fun getItemId(position: Int): Long {
        return fragmentList[position].hashCode().toLong()
    }

    override fun createFragment(position: Int): Fragment = fragmentList[position]

    override fun getItemCount(): Int = fragmentList.size

    fun getTitle(position: Int): String = fragmentTitle[position]

    fun getFragmentPosition(fragment: Fragment) : Int {
        return fragmentList.indexOf(fragment)
    }
}