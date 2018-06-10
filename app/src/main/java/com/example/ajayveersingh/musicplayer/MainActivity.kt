package com.example.ajayveersingh.musicplayer

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : AppCompatActivity()
{
    var drawer_layout:DrawerLayout?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var toolbar=findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawer_layout=findViewById(R.id.drawer_layout)

        val toggle=ActionBarDrawerToggle(this@MainActivity,drawer_layout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close)
        toggle.syncState()

        var mainscreenfragment=mainscreen_fragment()
        this.supportFragmentManager.beginTransaction().add(this@MainActivity,mainscreenfragment,"MainScreenFragment").commit()
    }
}