package com.storozhevykh.mygoings.adapters

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.storozhevykh.mygoings.Constants
import com.storozhevykh.mygoings.model.StaticStorage
import com.storozhevykh.mygoings.view.MainActivity
import com.storozhevykh.mygoings.view.NotificationsFragment
import com.storozhevykh.mygoings.view.PageFragment
import com.storozhevykh.mygoings.view.SettingsFragment

class ViewPagerAdapter(val activity: MainActivity, itemCount: Int, val mode: Int, val categoryFiltering: String, val sorting: String, val tabs: Array<String>) : FragmentStateAdapter(activity) {

    val count = itemCount

    override fun getItemCount(): Int {
        return count
    }

    override fun createFragment(position: Int): Fragment {
        if (mode == Constants.SHOW_MODE_SETTINGS) {
            activity.hideToolbarElements()
            if(position == 0)
            return SettingsFragment.newInstance()
            else
                return NotificationsFragment.newInstance()
        }
        else {
            activity.showToolbarElements()
            val pageFragment: PageFragment = PageFragment.newInstance(tabs[position], position, mode, categoryFiltering, sorting)
            //Log.d("mylog", "PageFragment is added, position: $position")
            StaticStorage.pageFragmentsList.add(pageFragment)
            return pageFragment
        }
    }

    companion object {
        fun updatePages(pagePosition: Int?) {
            if (!StaticStorage.pageFragmentsList.isEmpty()) {
                for (i in 0 until StaticStorage.pageFragmentsList.size) {
                    if (i != pagePosition)
                    StaticStorage.pageFragmentsList[i].updateList()
                }
            }
        }

        fun updateCategory(categoryFiltering: String) {
            for (i in 0 until StaticStorage.pageFragmentsList.size) {
                //Log.d("mylog", "PageFragment is updating, position: $i")
                    StaticStorage.pageFragmentsList[i].updateCategoryFiltering(categoryFiltering)
            }
        }

        fun updateSorting(sorting: String) {
            for (i in 0 until StaticStorage.pageFragmentsList.size) {
                Log.d("mylog", "PageFragment sorting is updating, position: $i")
                StaticStorage.pageFragmentsList[i].updateSorting(sorting)
            }
        }

    }

}
