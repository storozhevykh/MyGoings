package com.storozhevykh.mygoings

class Constants {
    companion object {

        val DATE_FORMAT = "dd MMM yyyy HH:mm"

        val PREF_NAME = "AppSettings"
        val PREF_THEME_KEY = "Theme"
        val PREF_NOTIFY_KEY = "Notify"
        val PREF_NOTIFY_PRIORITY = "NotifyPriority"
        val PREF_NOTIFY_MINUTES_KEY = "Minutes"

        //Themes
        val THEME_POSITIVE = "Positive"
        val THEME_BUSINESS = "Business"
        val THEME_BLACK_GOLD = "BlackGold"

        //Going states
        val GOING_STATE_ACTIVE = 0
        val GOING_STATE_DONE = 1
        val GOING_STATE_SKIPPED = 2

        // Categories
        val CATEGORY_NO_CATEGORY = "No category"
        val CATEGORY_SPORT = "Sport"
        val CATEGORY_HOME = "Home"
        val CATEGORY_WORK = "Work"
        val CATEGORY_FAMILY = "Family"

        //Priorities
        val PRIORITY_LOW = 0
        val PRIORITY_MEDIUM = 1
        val PRIORITY_HIGH = 2

        //Notifications
        val NOTIFY_LOW = PRIORITY_LOW
        val NOTIFY_MEDIUM = PRIORITY_MEDIUM
        val NOTIFY_HIGH = PRIORITY_HIGH

        //Filtering
        val FILTERING_TODAY = "Today"
        val FILTERING_TOMORROW = "Tomorrow"
        val FILTERING_ALL_ACTIVE = "All active"
        val FILTERING_IMPORTANT = "Important"
        val FILTERING_DONE = "Done"
        val FILTERING_EXPIRED = "Expired"
        val FILTERING_SKIPPED = "Skipped"
        val FILTERING_ALL_PAST = "All past"
        val FILTERING_CATEGORY_ALL = "All"
        val FILTERING_CATEGORY_WORK = "Work"
        val FILTERING_CATEGORY_FAMILY = "Family"
        val FILTERING_CATEGORY_HOME = "Home"
        val FILTERING_CATEGORY_SPORT = "Sport"
        val FILTERING_CATEGORY_NO = "No"

        //Sorting
        val SORT_CREATING = "Creating"
        val SORT_IMPORTANCE = "Importance"
        val SORT_DEADLINE = "Deadline"

        //Show modes
        val SHOW_MODE_IMPORTANT = 1;
        val SHOW_MODE_ACTIVE = 2;
        val SHOW_MODE_PAST = 3;
        val SHOW_MODE_SETTINGS = 4;

        //Adding modes
        val ADDING_MODE_NEW = 1;
        val ADDING_MODE_EDIT = 2;

        //Firebase
        val FIREBASE_KEY = "Going"
        val MODE_UPDATE = 0
        val MODE_REMOVE = 1
    }
}