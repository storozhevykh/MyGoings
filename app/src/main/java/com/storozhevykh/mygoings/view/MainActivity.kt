package com.storozhevykh.mygoings.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.AttrRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.storozhevykh.mygoings.App
import com.storozhevykh.mygoings.Constants
import com.storozhevykh.mygoings.R
import com.storozhevykh.mygoings.adapters.ViewPagerAdapter
import com.storozhevykh.mygoings.databinding.ActivityMainBinding
import com.storozhevykh.mygoings.firebase.SyncHelper
import com.storozhevykh.mygoings.model.FilterGoingsList
import com.storozhevykh.mygoings.model.StaticStorage
import com.storozhevykh.mygoings.services.NotificationService
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var filterGoingsList: FilterGoingsList
    @Inject
    lateinit var syncHelper: SyncHelper

    lateinit var binding: ActivityMainBinding

    lateinit var header: View
    lateinit var signOutBtn: TextView
    lateinit var userPhoto: ImageView
    lateinit var userName: TextView
    var categoryFiltering = Constants.FILTERING_CATEGORY_ALL
    var sorting = Constants.SORT_CREATING
    lateinit var sPref: SharedPreferences
    var curTheme = ""
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        App.component.inject(this)

        StaticStorage.activityContext = this

        sPref = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        if (sPref.getString(Constants.PREF_THEME_KEY, Constants.THEME_POSITIVE).equals(Constants.THEME_POSITIVE)) {
            setTheme(R.style.PositiveTheme)
            curTheme = Constants.THEME_POSITIVE
        }
        else if (sPref.getString(Constants.PREF_THEME_KEY, Constants.THEME_POSITIVE).equals(Constants.THEME_BUSINESS)) {
            setTheme(R.style.BusinessTheme)
            curTheme = Constants.THEME_BUSINESS
        }
        else if (sPref.getString(Constants.PREF_THEME_KEY, Constants.THEME_POSITIVE).equals(Constants.THEME_BLACK_GOLD)) {
            setTheme(R.style.BlackGoldTheme)
            curTheme = Constants.THEME_BLACK_GOLD
            //drawerLayout.setBackgroundColor(resources.getColor(R.color.blackGold_primaryVariant))
        }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        if (auth.currentUser != null) {
            StaticStorage.userEmail = Firebase.auth.currentUser?.email!!.replace(".", "")
            syncHelper.registerListener()
        }

        if (curTheme == Constants.THEME_BLACK_GOLD) {
            binding.drawerLayout.setBackgroundColor(resources.getColor(R.color.blackGold_primaryVariant))
            binding.tabLayout.background = getDrawable(R.drawable.tabs_background_gold)
            //binding.floatingActionButton.setBackgroundColor(resources.getColor(R.color.blackGold_primaryVariant))
        }

        initToolbar()
        initNavigationView()
        StaticStorage.pageFragmentsList = arrayListOf()
        if (StaticStorage.startShowMode == Constants.SHOW_MODE_SETTINGS)
            initViewPager(Constants.SHOW_MODE_SETTINGS, getString(R.string.manageGoings_tab), getString(R.string.manageNotifications_tab))
        else
            initViewPager(Constants.SHOW_MODE_ACTIVE, Constants.FILTERING_TODAY, Constants.FILTERING_TOMORROW, Constants.FILTERING_IMPORTANT, Constants.FILTERING_ALL_ACTIVE)

        startService(Intent(this, NotificationService::class.java))
    }

    override fun onStart() {
        super.onStart()
        navHeaderUpdate()
    }

    fun navHeaderUpdate() {
        val currentUser = auth.currentUser
        if(currentUser != null) {
            (header.findViewById<TextView>(R.id.signInBtn)).visibility = View.GONE
            (header.findViewById<TextView>(R.id.signInHeaderText)).visibility = View.GONE
            userPhoto.visibility = View.VISIBLE
            signOutBtn.visibility = View.VISIBLE
            userName.visibility = View.VISIBLE
            if (!auth.currentUser?.displayName.equals("") && auth.currentUser?.displayName != null) {
                userName.text = auth.currentUser?.displayName
                println("userName: ${auth.currentUser?.displayName}")
            }
            else
                userName.text = auth.currentUser?.email
            if (auth.currentUser?.photoUrl != null && !auth.currentUser?.photoUrl!!.equals(""))
            Glide.with(this).load(auth.currentUser?.photoUrl).circleCrop().into(userPhoto)
        }
        else {
            (header.findViewById<TextView>(R.id.signInBtn)).visibility = View.VISIBLE
            (header.findViewById<TextView>(R.id.signInHeaderText)).visibility = View.VISIBLE
            userPhoto.visibility = View.GONE
            signOutBtn.visibility = View.GONE
            userName.visibility = View.GONE
        }
    }

    private fun initViewPager(showMode: Int, vararg tabs: String) {
        StaticStorage.pageFragmentsList.clear()
        val tabTitles = arrayOf(*tabs)
        binding.toolbarTitle.text = resources.getStringArray(R.array.toolbar_titles)[showMode - 1]
        binding.viewPager.adapter = ViewPagerAdapter(this, tabTitles.size, showMode, categoryFiltering, sorting, tabTitles)

        if (curTheme.equals(Constants.THEME_BLACK_GOLD))
        binding.tabLayout.setSelectedTabIndicatorColor(resources.getColor(R.color.textColorGold))

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position -> tab.text = tabTitles[position]}.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    private fun initToolbar() {

        val categoryAdapter = commonSpinnerAdapter(resources.getStringArray(R.array.toolbar_category_menu))
        binding.categorySpinner.setAdapter(categoryAdapter)
        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> categoryFiltering = Constants.FILTERING_CATEGORY_ALL
                    1 -> categoryFiltering = Constants.FILTERING_CATEGORY_HOME
                    2 -> categoryFiltering = Constants.FILTERING_CATEGORY_FAMILY
                    3 -> categoryFiltering = Constants.FILTERING_CATEGORY_WORK
                    4 -> categoryFiltering = Constants.FILTERING_CATEGORY_SPORT
                    5 -> categoryFiltering = Constants.FILTERING_CATEGORY_NO
                }
                    //StaticStorage.currentGoingsListAdapter?.updateList(filterGoingsList.additionalFiltering(categoryFiltering, StaticStorage.goingsList) as MutableList<Going>)
                    //ViewPagerAdapter.updatePages(StaticStorage.currentGoingsListAdapter?.pagePosition)
                StaticStorage.categoryFiltering = categoryFiltering
                ViewPagerAdapter.updateCategory(categoryFiltering)
            }
        }

        val sortAdapter = commonSpinnerAdapter(resources.getStringArray(R.array.toolbar_sort_menu))
        binding.sortSpinner.setAdapter(sortAdapter)
        binding.sortSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> sorting = Constants.SORT_CREATING
                    1 -> sorting = Constants.SORT_IMPORTANCE
                    2 -> sorting = Constants.SORT_DEADLINE
                }
                ViewPagerAdapter.updateSorting(sorting)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        setSupportActionBar(binding.toolbar)
        getSupportActionBar()?.title = null
    }

    fun hideToolbarElements() {
        binding.categorySpinner.visibility = View.INVISIBLE
        binding.toolbarCategoryTv.visibility = View.INVISIBLE
        binding.sortSpinner.visibility = View.INVISIBLE
        binding.toolbarSortTv.visibility = View.INVISIBLE
    }

    fun showToolbarElements() {
        binding.categorySpinner.visibility = View.VISIBLE
        binding.toolbarCategoryTv.visibility = View.VISIBLE
        binding.sortSpinner.visibility = View.VISIBLE
        binding.toolbarSortTv.visibility = View.VISIBLE
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    private fun initNavigationView() {
        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.toggle_open, R.string.toggle_close)
        toggle.drawerArrowDrawable.color = themeColor(R.attr.colorOnPrimary)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        val navigationView: NavigationView = findViewById(R.id.navigation)
        header = navigationView.inflateHeaderView(R.layout.navigation_header)
        signOutBtn = header.findViewById(R.id.signOutBtn)
        userPhoto = header.findViewById(R.id.headerImage)
        userName = header.findViewById(R.id.headerUserName)
        signOutBtn.setOnClickListener {
            auth.signOut()
            syncHelper.unregisterListener()
            navHeaderUpdate()
        }

        Glide.with(this).load("https://cdn-icons-png.flaticon.com/512/149/149071.png").into(userPhoto)

        navigationView.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.important_item -> initViewPager(Constants.SHOW_MODE_IMPORTANT,
                    Constants.FILTERING_TODAY, Constants.FILTERING_TOMORROW, Constants.FILTERING_ALL_ACTIVE)
                R.id.active_item -> initViewPager(Constants.SHOW_MODE_ACTIVE,
                    Constants.FILTERING_TODAY, Constants.FILTERING_TOMORROW, Constants.FILTERING_IMPORTANT, Constants.FILTERING_ALL_ACTIVE)
                R.id.past_item -> initViewPager(Constants.SHOW_MODE_PAST,
                    Constants.FILTERING_DONE, Constants.FILTERING_EXPIRED, Constants.FILTERING_SKIPPED, Constants.FILTERING_ALL_PAST)
                R.id.settings_item -> initViewPager(Constants.SHOW_MODE_SETTINGS,
                    getString(R.string.manageGoings_tab), getString(R.string.manageNotifications_tab))
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return@OnNavigationItemSelectedListener true;
        })
    }

    fun floatingClick(view: View) {
        val dialog = AddingGoingDialog.newInstance(getString(R.string.adding_dialog_title), Constants.ADDING_MODE_NEW, -1, -1)
        dialog.show(supportFragmentManager, "TAG")
    }

    fun commonSpinnerAdapter (strArr: Array<String>): ArrayAdapter<String> {
        return object: ArrayAdapter<String>(applicationContext, R.layout.toolbar_spinner, strArr) {
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view:TextView = super.getDropDownView(position, convertView, parent) as TextView
                when (curTheme) {
                    Constants.THEME_POSITIVE -> view.setTextColor(resources.getColor(R.color.purple_700))
                    Constants.THEME_BUSINESS -> view.setTextColor(resources.getColor(R.color.textColorBusiness_spinner))
                    Constants.THEME_BLACK_GOLD -> view.setTextColor(resources.getColor(R.color.textColorGold_spinner))
                }
                return view
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view:TextView = super.getDropDownView(position, convertView, parent) as TextView
                when (curTheme) {
                    Constants.THEME_POSITIVE -> view.setTextColor(resources.getColor(R.color.purple_700))
                    Constants.THEME_BUSINESS -> view.setTextColor(resources.getColor(R.color.textColorBusiness_spinner))
                    Constants.THEME_BLACK_GOLD -> view.setTextColor(resources.getColor(R.color.textColorGold_spinner))
                }
                return view
            }
        }
    }

    fun themeColor(@AttrRes attrRes: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute (attrRes, typedValue, true)
        return typedValue.data
    }

    fun signHeaderClick(view: View) {
        val dialog = LoginDialog()
        dialog.show(supportFragmentManager, "TAG")
    }

    override fun onDestroy() {
        super.onDestroy()
        syncHelper.unregisterListener()
    }

}