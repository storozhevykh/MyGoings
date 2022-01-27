package com.storozhevykh.mygoings.model

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.storozhevykh.mygoings.Constants
import com.storozhevykh.mygoings.R
import com.storozhevykh.mygoings.adapters.GoingsListAdapter
import com.storozhevykh.mygoings.view.PageFragment

class StaticStorage {

    companion object {
        lateinit var recyclerView: RecyclerView
        lateinit var goingsList: List<Going>
        lateinit var pageFragmentsList: ArrayList<PageFragment>
        var userEmail: String? = null
        lateinit var appContext: Context
        lateinit var activityContext: AppCompatActivity
        var currentGoingsListAdapter: GoingsListAdapter? = null
        var categoryFiltering = Constants.FILTERING_CATEGORY_ALL
        var startShowMode = Constants.SHOW_MODE_ACTIVE

        val categoryDrawableIDs = listOf<HashMap<String, Int>>(
            hashMapOf<String, Int>(
                Constants.CATEGORY_NO_CATEGORY to R.drawable.icon_important_low,
                Constants.CATEGORY_HOME to R.drawable.icon_home_low,
                Constants.CATEGORY_FAMILY to R.drawable.icon_family_low,
                Constants.CATEGORY_WORK to R.drawable.icon_work_low,
                Constants.CATEGORY_SPORT to R.drawable.icon_sport_low),
            hashMapOf<String, Int>(
                Constants.CATEGORY_NO_CATEGORY to R.drawable.icon_important_medium,
                Constants.CATEGORY_HOME to R.drawable.icon_home_medium,
                Constants.CATEGORY_FAMILY to R.drawable.icon_family_medium,
                Constants.CATEGORY_WORK to R.drawable.icon_work_medium,
                Constants.CATEGORY_SPORT to R.drawable.icon_sport_medium),
            hashMapOf<String, Int>(
                Constants.CATEGORY_NO_CATEGORY to R.drawable.icon_important_high,
                Constants.CATEGORY_HOME to R.drawable.icon_home_high,
                Constants.CATEGORY_FAMILY to R.drawable.icon_family_high,
                Constants.CATEGORY_WORK to R.drawable.icon_work_high,
                Constants.CATEGORY_SPORT to R.drawable.icon_sport_high)
        )
    }
}