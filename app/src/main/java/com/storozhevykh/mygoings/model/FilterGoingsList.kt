package com.storozhevykh.mygoings.model

import com.storozhevykh.mygoings.App
import com.storozhevykh.mygoings.Constants
import com.storozhevykh.mygoings.database.DataBase
import com.storozhevykh.mygoings.database.GoingDao
import java.util.*
import java.util.stream.Stream
import javax.inject.Inject

class FilterGoingsList() {

    @Inject
    lateinit var dataBase: DataBase
    var goingDAO: GoingDao

    init {
        App.component.inject(this)
        goingDAO = dataBase.goingDao()
    }

    suspend fun getFiltered (filtering: String): List<Going> {
        val calendar: Calendar = Calendar.getInstance()
        var goingsList: List<Going> = goingDAO.getAll()
        when (filtering) {
            Constants.FILTERING_TODAY -> {
                changeTime(calendar, 2)
                goingsList = goingDAO.getTillTime(calendar.timeInMillis)
            }
            Constants.FILTERING_TOMORROW -> {
                changeTime(calendar, 3)
                goingsList = goingDAO.getTillTime(calendar.timeInMillis)
            }
            Constants.FILTERING_IMPORTANT -> goingsList = goingDAO.getWithPriority(Constants.PRIORITY_HIGH)
            Constants.FILTERING_EXPIRED -> goingsList = goingDAO.getTillTime(Date().time)
            Constants.FILTERING_DONE -> goingsList = goingDAO.getDone()
            Constants.FILTERING_SKIPPED -> goingsList = goingDAO.getSkipped()
            Constants.FILTERING_ALL_PAST -> goingsList = goingDAO.getPast(Date().time)
        }
        return goingsList
    }

    fun additionalFiltering (filtering: String, inputList: List<Going>): List<Going> {
        if (filtering == Constants.FILTERING_IMPORTANT)
            return inputList.filter { going -> going.priority == Constants.PRIORITY_HIGH }
                .filter { going -> going.state == Constants.GOING_STATE_ACTIVE }
                .filter { going -> going.timeElapsed > Date().time }
        else if (filtering == Constants.FILTERING_ALL_ACTIVE)
            return inputList.filter { going -> going.state == Constants.GOING_STATE_ACTIVE }
                .filter { going -> going.timeElapsed > Date().time }
        else if (filtering == Constants.FILTERING_CATEGORY_FAMILY)
            return inputList.filter { going -> going.category == Constants.CATEGORY_FAMILY }
        else if (filtering == Constants.FILTERING_CATEGORY_HOME)
            return inputList.filter { going -> going.category == Constants.CATEGORY_HOME }
        else if (filtering == Constants.FILTERING_CATEGORY_WORK)
            return inputList.filter { going -> going.category == Constants.CATEGORY_WORK }
        else if (filtering == Constants.FILTERING_CATEGORY_SPORT)
            return inputList.filter { going -> going.category == Constants.CATEGORY_SPORT }
        else if (filtering == Constants.FILTERING_CATEGORY_NO)
            return inputList.filter { going -> going.category == Constants.CATEGORY_NO_CATEGORY }
        else
            return inputList
    }

    private fun changeTime(calendar: Calendar, addDay: Int) {
        calendar.add(Calendar.DAY_OF_MONTH, addDay);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}