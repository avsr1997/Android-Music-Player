package com.example.ajayveersingh.musicplayer

import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar

class MainActivity : AppCompatActivity() {
    object statified {
        var drawer_layout: DrawerLayout? = null
    }

    var nav_drawer_list: ArrayList<String> = arrayListOf()
    var image_list: IntArray = intArrayOf(R.drawable.navigation_allsongs, R.drawable.navigation_favorites,
            R.drawable.navigation_settings, R.drawable.navigation_aboutus)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nav_drawer_list.add("All Songs")
        nav_drawer_list.add("Favourites")
        nav_drawer_list.add("Settings")
        nav_drawer_list.add("About Us")

        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        statified.drawer_layout = findViewById(R.id.drawer_layout)

        val toggle = ActionBarDrawerToggle(this@MainActivity, statified.drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        toggle.syncState()
        statified.drawer_layout?.addDrawerListener(toggle)

        var mainscreenfragment = mainscreen_fragment()
        this.supportFragmentManager.beginTransaction().add(R.id.fragment, mainscreenfragment, "MainScreenFragment").commit()

        var objnavAdapter = navAdapter(nav_drawer_list, image_list, this)
        objnavAdapter.notifyDataSetChanged()

        var recycler_view = findViewById<RecyclerView>(R.id.recycler_view)
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.itemAnimator = DefaultItemAnimator()
        recycler_view.adapter = objnavAdapter
        recycler_view.hasFixedSize()
    }
}