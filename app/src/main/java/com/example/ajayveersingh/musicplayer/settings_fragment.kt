package com.example.ajayveersingh.musicplayer


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Switch


class settings_fragment : Fragment() {

    var myactivity: Activity? = null
    var shakeswitch: Switch? = null

    object statified {
        var name = "shake_feature"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings_fragment, container, false)
        shakeswitch = view?.findViewById(R.id.switchShake)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myactivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myactivity = activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val mypres = myactivity?.getSharedPreferences(statified.name, Context.MODE_PRIVATE)
        val isAllowed = mypres?.getBoolean("feature", false)
        if (isAllowed as Boolean) {
            shakeswitch?.isChecked = true
        } else {
            shakeswitch?.isChecked = false
        }

        shakeswitch?.setOnCheckedChangeListener({ compoundButton, b ->
            if (b) {
                val editor = myactivity?.getSharedPreferences(statified.name, Context.MODE_PRIVATE)?.edit()
                editor?.putBoolean("feature", true)?.apply()
            } else {
                val editor = myactivity?.getSharedPreferences(statified.name, Context.MODE_PRIVATE)?.edit()
                editor?.putBoolean("feature", false)?.apply()
            }

        })
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
    }


}
