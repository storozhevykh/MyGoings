package com.storozhevykh.mygoings.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.storozhevykh.mygoings.R
import com.storozhevykh.mygoings.adapters.ViewPagerAdapter

class MainActivity : AppCompatActivity() {

    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var tabLayout: TabLayout

    private val SHOW_MODE_GOINGS = 1;
    private val SHOW_MODE_SETTINGS = 2;

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.DefaultTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initToolbar()
        initNavigationView()
        initViewPager(SHOW_MODE_GOINGS, "tab1", "tab2", "tab3")
    }

    private fun initViewPager(showMode: Int, vararg tabs: String) {
        val tabTitles = arrayOf(*tabs)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        viewPager.adapter = ViewPagerAdapter(this, tabTitles.size)
        tabLayout = findViewById(R.id.tabLayout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position -> tab.text = tabTitles[position]}.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.toolbar)
        toolbar.setTitle(R.string.app_name)
        setSupportActionBar(toolbar)
    }

    private fun initNavigationView() {
        drawerLayout = findViewById(R.id.drawerLayout)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.toggle_open, R.string.toggle_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        val navigationView: NavigationView = findViewById(R.id.navigation)

        navigationView.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.important_item -> initViewPager(SHOW_MODE_GOINGS,
                    getString(R.string.today_tab), getString(R.string.tomorrow_tab), getString(R.string.allActive_tab))
                R.id.active_item -> initViewPager(SHOW_MODE_GOINGS,
                    getString(R.string.today_tab), getString(R.string.tomorrow_tab), getString(R.string.important_tab), getString(R.string.allActive_tab))
                R.id.past_item -> initViewPager(SHOW_MODE_GOINGS,
                    getString(R.string.done_tab), getString(R.string.expired_tab), getString(R.string.skipped_tab), getString(R.string.allPast_tab))
                R.id.settings_item -> initViewPager(SHOW_MODE_SETTINGS,
                    getString(R.string.manageGoings_tab), getString(R.string.manageNotifications_tab))
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return@OnNavigationItemSelectedListener true;
        })
    }

    fun floatingClick(view: View) {
        val dialog = AddingGoingDialog()
        dialog.show(supportFragmentManager, "TAG")
    }
}