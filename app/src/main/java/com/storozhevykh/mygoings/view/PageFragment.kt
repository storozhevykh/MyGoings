package com.storozhevykh.mygoings.view

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.storozhevykh.mygoings.App
import com.storozhevykh.mygoings.Constants
import com.storozhevykh.mygoings.R
import com.storozhevykh.mygoings.adapters.GoingsListAdapter
import com.storozhevykh.mygoings.database.DataBase
import com.storozhevykh.mygoings.database.GoingDao
import com.storozhevykh.mygoings.model.FilterGoingsList
import com.storozhevykh.mygoings.model.Going
import com.storozhevykh.mygoings.model.StaticStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class PageFragment() : Fragment() {

    @Inject
    lateinit var dataBase: DataBase
    @Inject
    lateinit var filterGoingsList: FilterGoingsList

    lateinit var goingDAO: GoingDao
    private val calendar: Calendar = Calendar.getInstance()
    lateinit var recyclerView: RecyclerView
    lateinit var filteredGoings: MutableList<Going>

    private var filtering = Constants.FILTERING_ALL_ACTIVE
    private var pagePosition = 0
    private var mainMenuItem = 0
    private var categoryFiltering = Constants.FILTERING_CATEGORY_ALL
    private var sorting = Constants.SORT_CREATING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            filtering = it.getString(FILTERING)!!
            pagePosition = it.getInt(POSITION)
            mainMenuItem = it.getInt(MODE)
            categoryFiltering = it.getString(CATEGORY_FILTERING)!!
            sorting = it.getString(SORTING)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        App.component.inject(this)
        goingDAO = dataBase.goingDao()

        val view: View = inflater.inflate(R.layout.fragment_page, container, false)
        //val text: TextView? = view.findViewById(R.id.tv_fragment)
        recyclerView = view.findViewById(R.id.recyclerView)
        StaticStorage.recyclerView = recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        updateList()

        return view
    }

    fun updateCategoryFiltering (categoryFiltering: String) {
        this.categoryFiltering = categoryFiltering
        updateList()
    }

    fun updateSorting (sorting: String) {
        this.sorting = sorting
        updateList()
    }

    fun updateList() {
        CoroutineScope(Dispatchers.Main).launch {
            filteredGoings = filterGoingsList.getFiltered(filtering).toMutableList()

            if (mainMenuItem == Constants.SHOW_MODE_IMPORTANT)
                filteredGoings = filterGoingsList.additionalFiltering(Constants.FILTERING_IMPORTANT, filteredGoings) as MutableList<Going>

            if (mainMenuItem == Constants.SHOW_MODE_ACTIVE)
                filteredGoings = filterGoingsList.additionalFiltering(Constants.FILTERING_ALL_ACTIVE, filteredGoings) as MutableList<Going>

            filteredGoings = filterGoingsList.additionalFiltering(categoryFiltering, filteredGoings) as MutableList<Going>

            when (sorting) {
                Constants.SORT_CREATING -> filteredGoings.sortBy { it.timeCreated }
                Constants.SORT_IMPORTANCE -> filteredGoings.sortWith(compareByDescending<Going> ({ it.priority }).thenBy { it.timeElapsed })
                Constants.SORT_DEADLINE -> filteredGoings.sortWith(compareBy<Going> ({ it.timeElapsed }, { it.priority }, { it.timeCreated }))
            }
            StaticStorage.goingsList = filteredGoings
            /*val curTime = Date().time
            var rightBound = filteredGoings.size
            filteredGoings.removeAll { mainMenuItem == Constants.SHOW_MODE_ACTIVE && it.timeElapsed < curTime }
            filteredGoings.removeAll { mainMenuItem == Constants.SHOW_MODE_PAST && it.timeElapsed >= curTime }
            filteredGoings.removeAll { mainMenuItem == Constants.SHOW_MODE_IMPORTANT && it.priority != Constants.PRIORITY_HIGH }*/
            Log.d("mylog", "updateList(): ${filteredGoings.size}")
            recyclerView.adapter = GoingsListAdapter(filteredGoings, pagePosition, activity)
        }
    }

    private fun changeTime(calendar: Calendar, addDay: Int) {
        calendar.add(Calendar.DAY_OF_MONTH, addDay);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    companion object {

        private val FILTERING = "FILTERING"
        private val POSITION = "POSITION"
        private val MODE = "MODE"
        private val CATEGORY_FILTERING = "CATEGORY_FILTERING"
        private val SORTING = "SORTING"

        @JvmStatic
        fun newInstance(filtering: String, position: Int, mode: Int, categoryFiltering: String, sorting: String) : PageFragment {
            val fragment = PageFragment()
            fragment.arguments = bundleOf(
                FILTERING to filtering,
                POSITION to position,
                MODE to mode,
                CATEGORY_FILTERING to categoryFiltering,
                SORTING to sorting
            )
            return fragment
        }
    }
}