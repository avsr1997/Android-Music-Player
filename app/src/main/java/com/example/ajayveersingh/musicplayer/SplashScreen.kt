package com.example.ajayveersingh.musicplayer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

class SplashScreen : AppCompatActivity() {

    var permissionString=arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.PROCESS_OUTGOING_CALLS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        if(!haspermission(this@SplashScreen,*permissionString))
        {
            ActivityCompat.requestPermissions(this@SplashScreen,permissionString,131)
        }
        else
        {
            Handler().postDelayed({
                var intent1= Intent(this@SplashScreen,MainActivity::class.java)
                startActivity(intent1)
                this.finish()
            },1000)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode)
        {
            131->{
                if(grantResults.isNotEmpty()&& grantResults[0]== PackageManager.PERMISSION_GRANTED
                        && grantResults[1]== PackageManager.PERMISSION_GRANTED
                        && grantResults[2]== PackageManager.PERMISSION_GRANTED
                        && grantResults[3]== PackageManager.PERMISSION_GRANTED
                        && grantResults[4]== PackageManager.PERMISSION_GRANTED)
                {
                    Handler().postDelayed({
                        var intent1= Intent(this@SplashScreen,MainActivity::class.java)
                        startActivity(intent1)
                        this.finish()
                    },1000)
                }
                else
                {
                    Toast.makeText(this@SplashScreen,"Grant All Permissions", Toast.LENGTH_SHORT).show()
                    this.finish()
                }

            }
            else->
            {
                Toast.makeText(this@SplashScreen,"Error Occured", Toast.LENGTH_SHORT).show()
                this.finish()
            }
        }
    }


    fun haspermission(context: Context, vararg permission:String):Boolean
    {
        var a=true
        for(b in permission)
        {
            var c=context.checkCallingOrSelfPermission(b)
            if(c!= PackageManager.PERMISSION_GRANTED)
            {
                a=false
            }
        }
        return a

    }
}
