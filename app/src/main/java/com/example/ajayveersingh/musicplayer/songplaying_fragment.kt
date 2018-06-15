package com.example.ajayveersingh.musicplayer


import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.example.ajayveersingh.musicplayer.songplaying_fragment.static.myactivity
import com.example.ajayveersingh.musicplayer.songplaying_fragment.static.songPosition
import kotlinx.android.synthetic.main.fragment_songplaying_fragment.*
import java.util.*
import java.util.concurrent.TimeUnit

class songplaying_fragment : Fragment() {

    var macceleration: Float = 0f
    var maccelerationcurrent: Float = 0f
    var maccelerationlast: Float = 0f


    object static {

        var startTime: TextView? = null
        var endTime: TextView? = null
        var playpause_button: ImageButton? = null
        var previous_button: ImageButton? = null
        var next_button: ImageButton? = null
        var loop_button: ImageButton? = null
        var shuffleButton: ImageButton? = null
        var song_Title: TextView? = null
        var songartist: TextView? = null
        var seekbar: SeekBar? = null
        var myhelper = helper()
        var myactivity: Activity? = null
        var mediaPlayer: MediaPlayer? = null
        var songPosition: Int = 0
        var songDetails: ArrayList<Songs>? = null
        var audioVisualization: AudioVisualization? = null
        var glview: GLAudioVisualizationView? = null
        var fb: ImageButton? = null
        var fav_database: favorite_database? = null
        var msensorManager: SensorManager? = null
        var msensorListener: SensorEventListener? = null
        var MY_PREFS_NAME = "shake_feature"

        var updatesongtime = object : Runnable {
            override fun run() {
                var getcurrent = static.mediaPlayer?.currentPosition
                static.startTime?.setText(String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(getcurrent?.toLong() as Long),
                        TimeUnit.MILLISECONDS.toSeconds(getcurrent?.toLong() as Long)))
                seekbar?.setProgress(getcurrent?.toInt() as Int)
                Handler().postDelayed(this, 1000)
            }

        }
    }


    object stat {
        var myshuffle: String = "data_shuffle"
        var myloop: String = "data_loop"
        fun updatetextviews(name: String, artist: String) {
            static.song_Title?.setText(name)
            static.songartist?.setText(artist)
        }

        fun processinformation(mediaPlayer: MediaPlayer) {
            val final_time = mediaPlayer.duration
            val start_time = mediaPlayer.currentPosition
            static.seekbar?.max = final_time
            static.startTime?.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(start_time.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(final_time.toLong())))
            static.endTime?.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(start_time.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(final_time.toLong())))

            static.seekbar?.setProgress(start_time)
            Handler().postDelayed(static.updatesongtime, 1000)

        }

        fun onsongComplete() {
            if (static.myhelper.isloop as Boolean) {
                nextsong("Shuffle Next")
            } else {
                nextsong("Normal Next")
            }
            processinformation(static.mediaPlayer as MediaPlayer)
            if (static.fav_database?.checkidexists(static.myhelper.nowsongID as Int) as Boolean) {
                static.fb?.setBackgroundResource(R.drawable.favorite_on)
            } else {
                static.fb?.setBackgroundResource((R.drawable.favorite_off))
            }
        }

        fun nextsong(check: String) {
            if (check.equals("Normal Next")) {
                songPosition += 1

            } else if (check.equals("Shuffle Next")) {
                var random = Random()
                var randomposition = random.nextInt(static.songDetails?.size?.plus(1) as Int)
                songPosition = randomposition
            }
            if (songPosition == static.songDetails?.size) {
                songPosition = 0
            }
            var nextsong = static.songDetails?.get(songPosition)
            static.myhelper.nowsongpath = nextsong?.songData
            static.myhelper.nowartist = nextsong?.artist
            static.myhelper.nowsongID = nextsong?.songID!!
            static.myhelper.nowsongtitle = nextsong?.songTitle
            static.myhelper.nowposition = songPosition
            static.mediaPlayer?.reset()
            try {
                static.mediaPlayer?.setDataSource(myactivity, Uri.parse(static.myhelper.nowsongpath))
                static.mediaPlayer?.prepare()
                static.mediaPlayer?.start()
                processinformation(static.mediaPlayer as MediaPlayer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            updatetextviews(static.myhelper.nowsongtitle as String, static.myhelper.nowartist as String)
            if (static.fav_database?.checkidexists(static.myhelper.nowsongID as Int) as Boolean) {
                static.fb?.setBackgroundResource(R.drawable.favorite_on)
            } else {
                static.fb?.setBackgroundResource((R.drawable.favorite_off))
            }
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_songplaying_fragment, container, false)
        static.startTime = view?.findViewById(R.id.start_time)
        static.endTime = view?.findViewById(R.id.end_time)
        static.playpause_button = view?.findViewById(R.id.playpause_button)
        static.previous_button = view?.findViewById(R.id.previousbutton)
        static.next_button = view?.findViewById(R.id.next_button)
        static.loop_button = view?.findViewById(R.id.loop_button)
        static.shuffleButton = view?.findViewById(R.id.shuffle_button)
        static.song_Title = view?.findViewById(R.id.songTitle)
        static.songartist = view?.findViewById(R.id.songArtist)
        static.seekbar = view?.findViewById(R.id.seekbar)
        static.glview = view?.findViewById(R.id.visualizer_view)
        static.fb = view?.findViewById(R.id.favorites)
        static.fav_database = favorite_database(myactivity)
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

        static.myhelper.isplaying = true
        static.myhelper.isloop = false
        static.myhelper.isshuffle = false
        super.onActivityCreated(savedInstanceState)
        var path: String? = null
        var _songTitle: String? = null
        var _songArtist: String? = null
        var songId: Long = 0

        try {
            path = arguments?.getString("path")
            _songTitle = arguments?.getString("songTitle")
            _songArtist = arguments?.getString("songA rtist")
            songId = arguments?.getInt("songId")!!.toLong()
            static.songPosition = arguments?.getInt("songPosition")!!
            static.songDetails = arguments?.getParcelableArrayList("songData")
            static.myhelper.nowsongpath = path
            static.myhelper.nowartist = _songArtist
            static.myhelper.nowsongID = songId
            static.myhelper.nowsongtitle = _songTitle
            static.myhelper.nowposition = songPosition

        } catch (e: Exception) {
            e.printStackTrace()
        }
        static.mediaPlayer = MediaPlayer()
        static.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        try {
            static.mediaPlayer?.setDataSource(myactivity, Uri.parse(path))
            static.mediaPlayer?.prepare()

        } catch (e: Exception) {
            e.printStackTrace()
        }
        static.mediaPlayer?.start()
        stat.processinformation(static.mediaPlayer as MediaPlayer)
        clickhandler()
        stat.onsongComplete()
        var visualizationhandler = DbmHandler.Factory.newVisualizerHandler(myactivity as Context, 0)
        static.audioVisualization?.linkTo(visualizationhandler)

        var for_shuffle = myactivity?.getSharedPreferences(stat.myshuffle, Context.MODE_PRIVATE)
        for_shuffle?.getBoolean("value", false)

        var for_loop = myactivity?.getSharedPreferences(stat.myshuffle, Context.MODE_PRIVATE)
        for_loop?.getBoolean("value", false)

        if (static.fav_database?.checkidexists(static.myhelper.nowsongID as Int) as Boolean) {
            static.fb?.setBackgroundResource(R.drawable.favorite_on)
        } else {
            static.fb?.setBackgroundResource((R.drawable.favorite_off))
        }
    }

    override fun onResume() {
        super.onResume()
        static.audioVisualization?.onResume()
        static.msensorManager?.registerListener(static.msensorListener, static.msensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        static.audioVisualization?.onPause()
        static.msensorManager?.unregisterListener(static.msensorListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        static.audioVisualization?.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        static.msensorManager = static.myactivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        macceleration = 0.0f
        maccelerationcurrent = SensorManager.GRAVITY_EARTH
        maccelerationlast = SensorManager.GRAVITY_EARTH
        bindshake_listener()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_redirect -> {
                static.myactivity?.onBackPressed()
                return false
            }
        }
        return false
    }

    fun clickhandler() {

        static.fb?.setOnClickListener({
            if (static.fav_database?.checkidexists(static.myhelper.nowsongID.toInt()) as Boolean) {
                static.fav_database?.deletesong_database(static.myhelper.nowsongID as Int)
                static.fb?.setBackgroundResource(R.drawable.favorite_off)
            } else {
                static.fav_database?.storefavoritesong(static.myhelper.nowsongID as Int, static.myhelper.nowartist, static.myhelper.nowsongpath, static.myhelper.nowsongtitle)
                static.fb?.setBackgroundResource(R.drawable.favorite_on)
            }
        })
        playpause_button?.setOnClickListener({
            if (static.mediaPlayer?.isPlaying as Boolean) {
                static.mediaPlayer?.pause()
                playpause_button?.setBackgroundResource(R.drawable.play_icon)
            } else {
                static.mediaPlayer?.start()
                playpause_button?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
        next_button?.setOnClickListener({
            if (static.myhelper.isshuffle as Boolean) {
                stat.nextsong("Normal Next")
            } else {
                stat.nextsong("Shuffle Next")
            }
        })
        static.previous_button?.setOnClickListener({
            previous_song()
        })
        static.shuffleButton?.setOnClickListener({
            var edit_shuffle = myactivity?.getSharedPreferences(stat.myshuffle, Context.MODE_PRIVATE)?.edit()

            if (static.myhelper.isshuffle as Boolean) {
                static.myhelper.isshuffle = true
                static.shuffleButton?.setBackgroundResource(R.drawable.shuffle_icon)
                edit_shuffle?.putBoolean("value", true)
                edit_shuffle?.apply()
            } else {
                static.myhelper.isshuffle = false
                static.shuffleButton?.setBackgroundResource((R.drawable.shuffle_white_icon))
                edit_shuffle?.putBoolean("value", false)
            }

        })
        loop_button?.setOnClickListener({
            var edit_loop = myactivity?.getSharedPreferences(stat.myloop, Context.MODE_PRIVATE)?.edit()
            if (static.myhelper.isloop as Boolean) {
                static.myhelper.isloop = true
                loop_button?.setBackgroundResource(R.drawable.shuffle_icon)
                edit_loop?.putBoolean("value", true)
                edit_loop?.apply()
            } else {
                static.myhelper.isloop = false
                loop_button?.setBackgroundResource(R.drawable.loop_white_icon)
                edit_loop?.putBoolean("value", false)
                edit_loop?.apply()
            }

        })
    }


    fun previous_song() {
        songPosition -= 1
        if (songPosition == -1) {
            songPosition = 0
        }
        static.mediaPlayer?.reset()
        var nextsong = static.songDetails?.get(songPosition)
        static.myhelper.nowsongpath = nextsong?.songData
        static.myhelper.nowartist = nextsong?.artist
        static.myhelper.nowsongID = nextsong?.songID!!
        static.myhelper.nowsongtitle = nextsong?.songTitle
        static.myhelper.nowposition = songPosition
        try {
            static.mediaPlayer?.setDataSource(myactivity, Uri.parse(static.myhelper.nowsongpath))
            static.mediaPlayer?.prepare()
            static.mediaPlayer?.start()
            stat.processinformation(static.mediaPlayer as MediaPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stat.updatetextviews(static.myhelper.nowsongtitle as String, static.myhelper.nowartist as String)
        if (static.fav_database?.checkidexists(static.myhelper.nowsongID as Int) as Boolean) {
            static.fb?.setBackgroundResource(R.drawable.favorite_on)
        } else {
            static.fb?.setBackgroundResource((R.drawable.favorite_off))
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        static.audioVisualization = static.glview as AudioVisualization
    }

    fun bindshake_listener() {
        static.msensorListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }

            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                maccelerationlast = maccelerationcurrent
                maccelerationcurrent = Math.sqrt(((x * x + y * y + z * z).toDouble())).toFloat()
                val delta = maccelerationcurrent - maccelerationlast
                macceleration = macceleration * 0.9f + delta

                if (macceleration > 12) {
                    var myprefs = myactivity?.getSharedPreferences(static.MY_PREFS_NAME, Context.MODE_PRIVATE)
                    val isAllowed = myprefs?.getBoolean("featrue", false)
                    if (isAllowed as Boolean) {
                        stat.nextsong("PlaynextNormal")
                    }

                }
            }

        }
    }
}