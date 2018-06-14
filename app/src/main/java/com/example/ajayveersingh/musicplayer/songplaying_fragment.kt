package com.example.ajayveersingh.musicplayer


import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import kotlinx.android.synthetic.main.fragment_songplaying_fragment.*
import java.util.*
import java.util.concurrent.TimeUnit

class songplaying_fragment : Fragment() {
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
    var updatesongtime = object : Runnable {
        override fun run() {
            var getcurrent = mediaPlayer?.currentPosition
            startTime?.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(getcurrent?.toLong() as Long),
                    TimeUnit.MILLISECONDS.toSeconds(getcurrent?.toLong() as Long)))
            seekbar?.setProgress(getcurrent?.toInt() as Int)
            Handler().postDelayed(this, 1000)
        }

    }

    object static {
        var myshuffle: String = "data_shuffle"
        var myloop: String = "data_loop"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_songplaying_fragment, container, false)
        startTime = view?.findViewById(R.id.start_time)
        endTime = view?.findViewById(R.id.end_time)
        playpause_button = view?.findViewById(R.id.playpause_button)
        previous_button = view?.findViewById(R.id.previousbutton)
        next_button = view?.findViewById(R.id.next_button)
        loop_button = view?.findViewById(R.id.loop_button)
        shuffleButton = view?.findViewById(R.id.shuffle_button)
        songTitle = view?.findViewById(R.id.songTitle)
        songartist = view?.findViewById(R.id.songArtist)
        seekbar = view?.findViewById(R.id.seekbar)
        glview = view?.findViewById(R.id.visualizer_view)
        fb = view?.findViewById(R.id.favorites)
        fav_database = favorite_database(myactivity)
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

        myhelper.isplaying = true
        myhelper.isloop = false
        myhelper.isshuffle = false
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
            songPosition = arguments?.getInt("songPosition")!!
            songDetails = arguments?.getParcelableArrayList("songData")
            myhelper.nowsongpath = path
            myhelper.nowartist = _songArtist
            myhelper.nowsongID = songId
            myhelper.nowsongtitle = _songTitle
            myhelper.nowposition = songPosition

        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        try {
            mediaPlayer?.setDataSource(myactivity, Uri.parse(path))
            mediaPlayer?.prepare()

        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer?.start()
        processinformation(mediaPlayer as MediaPlayer)
        clickhandler()
        onsongComplete()
        var visualizationhandler = DbmHandler.Factory.newVisualizerHandler(myactivity as Context, 0)
        audioVisualization?.linkTo(visualizationhandler)

        var for_shuffle = myactivity?.getSharedPreferences(static.myshuffle, Context.MODE_PRIVATE)
        for_shuffle?.getBoolean("value", false)

        var for_loop = myactivity?.getSharedPreferences(static.myshuffle, Context.MODE_PRIVATE)
        for_loop?.getBoolean("value", false)

        if (fav_database?.checkidexists(myhelper.nowsongID as Int) as Boolean) {
            fb?.setBackgroundResource(R.drawable.favorite_on)
        } else {
            fb?.setBackgroundResource((R.drawable.favorite_off))
        }
    }

    override fun onResume() {
        super.onResume()
        audioVisualization?.onResume()
    }

    override fun onPause() {
        super.onPause()
        audioVisualization?.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioVisualization?.release()
    }

    fun clickhandler() {

        fb?.setOnClickListener({
            if (fav_database?.checkidexists(myhelper.nowsongID.toInt()) as Boolean) {
                fav_database?.deletesong_database(myhelper.nowsongID as Int)
                fb?.setBackgroundResource(R.drawable.favorite_off)
            } else {
                fav_database?.storefavoritesong(myhelper.nowsongID as Int, myhelper.nowartist, myhelper.nowsongpath, myhelper.nowsongtitle)
                fb?.setBackgroundResource(R.drawable.favorite_on)
            }
        })
        playpause_button?.setOnClickListener({
            if (mediaPlayer?.isPlaying as Boolean) {
                mediaPlayer?.pause()
                playpause_button?.setBackgroundResource(R.drawable.play_icon)
            } else {
                mediaPlayer?.start()
                playpause_button?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
        next_button?.setOnClickListener({
            if (myhelper.isshuffle as Boolean) {
                nextsong("Normal Next")
            } else {
                nextsong("Shuffle Next")
            }
        })
        previous_button?.setOnClickListener({
            previous_song()
        })
        shuffleButton?.setOnClickListener({
            var edit_shuffle = myactivity?.getSharedPreferences(static.myshuffle, Context.MODE_PRIVATE)?.edit()

            if (myhelper.isshuffle as Boolean) {
                myhelper.isshuffle = true
                shuffleButton?.setBackgroundResource(R.drawable.shuffle_icon)
                edit_shuffle?.putBoolean("value", true)
                edit_shuffle?.apply()
            } else {
                myhelper.isshuffle = false
                shuffleButton?.setBackgroundResource((R.drawable.shuffle_white_icon))
                edit_shuffle?.putBoolean("value", false)
            }

        })
        loop_button?.setOnClickListener({
            var edit_loop = myactivity?.getSharedPreferences(static.myloop, Context.MODE_PRIVATE)?.edit()
            if (myhelper.isloop as Boolean) {
                myhelper.isloop = true
                loop_button?.setBackgroundResource(R.drawable.shuffle_icon)
                edit_loop?.putBoolean("value", true)
                edit_loop?.apply()
            } else {
                myhelper.isloop = false
                loop_button?.setBackgroundResource(R.drawable.loop_white_icon)
                edit_loop?.putBoolean("value", false)
                edit_loop?.apply()
            }

        })
    }

    fun nextsong(check: String) {
        if (check.equals("Normal Next")) {
            songPosition += 1

        } else if (check.equals("Shuffle Next")) {
            var random = Random()
            var randomposition = random.nextInt(songDetails?.size?.plus(1) as Int)
            songPosition = randomposition
        }
        if (songPosition == songDetails?.size) {
            songPosition = 0
        }
        var nextsong = songDetails?.get(songPosition)
        myhelper.nowsongpath = nextsong?.songData
        myhelper.nowartist = nextsong?.artist
        myhelper.nowsongID = nextsong?.songID!!
        myhelper.nowsongtitle = nextsong?.songTitle
        myhelper.nowposition = songPosition
        mediaPlayer?.reset()
        try {
            mediaPlayer?.setDataSource(myactivity, Uri.parse(myhelper.nowsongpath))
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            processinformation(mediaPlayer as MediaPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        updatetextviews(myhelper.nowsongtitle as String, myhelper.nowartist as String)
        if (fav_database?.checkidexists(myhelper.nowsongID as Int) as Boolean) {
            fb?.setBackgroundResource(R.drawable.favorite_on)
        } else {
            fb?.setBackgroundResource((R.drawable.favorite_off))
        }
    }

    fun previous_song() {
        songPosition -= 1
        if (songPosition == -1) {
            songPosition = 0
        }
        mediaPlayer?.reset()
        var nextsong = songDetails?.get(songPosition)
        myhelper.nowsongpath = nextsong?.songData
        myhelper.nowartist = nextsong?.artist
        myhelper.nowsongID = nextsong?.songID!!
        myhelper.nowsongtitle = nextsong?.songTitle
        myhelper.nowposition = songPosition
        try {
            mediaPlayer?.setDataSource(myactivity, Uri.parse(myhelper.nowsongpath))
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            processinformation(mediaPlayer as MediaPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        updatetextviews(myhelper.nowsongtitle as String, myhelper.nowartist as String)
        if (fav_database?.checkidexists(myhelper.nowsongID as Int) as Boolean) {
            fb?.setBackgroundResource(R.drawable.favorite_on)
        } else {
            fb?.setBackgroundResource((R.drawable.favorite_off))
        }

    }

    fun onsongComplete() {
        if (myhelper.isloop as Boolean) {
            nextsong("Shuffle Next")
        } else {
            nextsong("Normal Next")
        }
        processinformation(mediaPlayer as MediaPlayer)
        if (fav_database?.checkidexists(myhelper.nowsongID as Int) as Boolean) {
            fb?.setBackgroundResource(R.drawable.favorite_on)
        } else {
            fb?.setBackgroundResource((R.drawable.favorite_off))
        }
    }

    fun updatetextviews(name: String, artist: String) {
        song_Title?.setText(name)
        songartist?.setText(artist)
    }

    fun processinformation(mediaPlayer: MediaPlayer) {
        val final_time = mediaPlayer.duration
        val start_time = mediaPlayer.currentPosition
        seekbar?.max = final_time
        startTime?.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(start_time.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(final_time.toLong())))
        endTime?.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(start_time.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(final_time.toLong())))

        seekbar?.setProgress(start_time)
        Handler().postDelayed(updatesongtime, 1000)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioVisualization = glview as AudioVisualization
    }
}