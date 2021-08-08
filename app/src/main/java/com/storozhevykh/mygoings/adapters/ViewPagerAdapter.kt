package com.storozhevykh.mygoings.adapters

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.storozhevykh.mygoings.view.PageFragment

class ViewPagerAdapter(activity: FragmentActivity, itemCount: Int) : FragmentStateAdapter(activity) {

    val count = itemCount

    override fun getItemCount(): Int {
        return count
    }

    override fun createFragment(position: Int): Fragment {
        return PageFragment.newInstance(position.toString(), position.toString())
    }

}
