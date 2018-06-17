package com.example.ajayveersingh.musicplayer


import android.app.Activity
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler

import android.support.v4.app.Fragment
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import kotlinx.android.synthetic.main.fragment_favrouites_fragment.*
import kotlinx.android.synthetic.main.fragment_songplaying_fragment.*
import org.w3c.dom.Text
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class songplaying_fragment : Fragment() {

    var myactivity: Activity? = null
    var mediaPlayer: MediaPlayer? = null
    var audioVisualization: AudioVisualization? = null
    var glview: GLAudioVisualizationView? = null
    var starttimetext: TextView? = null
    var endtimetext: TextView? = null
    var playpausebutton: ImageButton? = null
    var previousbutton: ImageButton? = null
    var nextbutton: ImageButton? = null
    var loopbutton: ImageButton? = null
    var shufflebutton: ImageButton? = null
    var seekbar: SeekBar? = null
    var songartistview: TextView? = null
    var songTitleview: TextView? = null
    var currentSongHelper: CurrentSongHelper? = null
    var currentposition: Int = 0
    var fetchsongs: ArrayList<Songs>? = null
    var fab: ImageButton? = null

    object staticated {
        val MY_PREFS_SHUFFLE = "Shuffle Feature"
        val MY_PREFS_LOOP = "loop feature"
    }

    var favoriteContent: favorite_database? = null
    var updatesongtime = object : Runnable {
        override fun run() {
            var getcurrent = mediaPlayer?.currentPosition
            starttimetext?.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(getcurrent?.toLong() as Long),
                    TimeUnit.MILLISECONDS.toSeconds(getcurrent?.toLong() as Long) - TimeUnit.MILLISECONDS
                            .toSeconds(TimeUnit.MILLISECONDS
                                    .toMinutes(getcurrent?.toLong() as Long))))
            Handler().postDelayed(this, 1000)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_songplaying_fragment, container, false)
        seekbar = view?.findViewById(R.id.seekbar)
        starttimetext = view?.findViewById(R.id.start_time)
        endtimetext = view?.findViewById(R.id.end_time)
        playpausebutton = view?.findViewById(R.id.playpause_button)
        nextbutton = view?.findViewById(R.id.next_button)
        previousbutton = view?.findViewById(R.id.previousbutton)
        loopbutton = view?.findViewById(R.id.loop_button)
        shufflebutton = view?.findViewById(R.id.shuffle_button)
        songTitleview = view?.findViewById(R.id.songTitle)
        songartistview = view?.findViewById(R.id.songArtist)
        glview = view?.findViewById(R.id.visualizer_view)
        fab = view?.findViewById(R.id.favorites)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioVisualization = glview as AudioVisualization
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

        favoriteContent = favorite_database(myactivity)

        currentSongHelper = CurrentSongHelper()
        currentSongHelper?.isplaying = true
        currentSongHelper?.isloop = false
        currentSongHelper?.isshuffle = false
        var path: String? = null
        var _songTitle: String? = null
        var _songArtist: String? = null
        var songID: Long = 0

        try {
            path = arguments?.getString("path")
            _songTitle = arguments?.getString("songTitle")
            _songArtist = arguments?.getString("songArtist")
            songID = arguments?.getInt("songId")!!.toLong()
            currentposition = arguments?.getInt("songPosition") as Int
            fetchsongs = arguments?.getParcelableArrayList("songData")

            currentSongHelper?.currentPosition = currentposition
            currentSongHelper?.songPath = path
            currentSongHelper?.songID = songID
            currentSongHelper?.songTitle = _songTitle
            currentSongHelper?.songArtist = _songArtist
            updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)

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
        processInformation(mediaPlayer as MediaPlayer)
        if (currentSongHelper?.isplaying as Boolean) {
            playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
        }
        mediaPlayer?.setOnCompletionListener({
            onsongComplete()
        })



        clickhandler()
        var visualizerHandler = DbmHandler.Factory.newVisualizerHandler(myactivity as Context, 0)
        audioVisualization?.linkTo(visualizerHandler)

        var prefsForShuffle = myactivity?.getSharedPreferences(staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature", false)
        if (isShuffleAllowed as Boolean) {
            currentSongHelper?.isshuffle = true
            currentSongHelper?.isloop = false
            shufflebutton?.setBackgroundResource(R.drawable.shuffle_icon)
            loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)

        } else {
            currentSongHelper?.isshuffle = false
            shufflebutton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }

        var prefsForLoop = myactivity?.getSharedPreferences(staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForShuffle?.getBoolean("feature", false)
        if (isShuffleAllowed as Boolean) {
            currentSongHelper?.isshuffle = false
            currentSongHelper?.isloop = true
            shufflebutton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            loopbutton?.setBackgroundResource(R.drawable.loop_icon)

        } else {
            currentSongHelper?.isloop = false
            loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)
        }
        /*if (favoriteContent?.checkidexists(currentSongHelper?.songID?.toInt() as Int) as Boolean) {
            fab?.setBackgroundResource(R.drawable.favorite_on)
        } else {
            fab?.setBackgroundResource(R.drawable.favorite_off)
        }*/
    }


    override fun onPause() {
        super.onPause()
        audioVisualization?.onPause()
    }

    override fun onResume() {
        super.onResume()
        audioVisualization?.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioVisualization?.release()
    }


    fun clickhandler() {
        fab?.setOnClickListener({
            if (favoriteContent?.checkidexists(currentSongHelper?.songID?.toInt() as Int) as Boolean) {
                fab?.setBackgroundResource(R.drawable.favorite_off)
                favoriteContent?.deletesong_database(currentSongHelper?.songID as Int)
                Toast.makeText(myactivity, "Removed from favorites", Toast.LENGTH_SHORT).show()
            } else {
                fab?.setBackgroundResource(R.drawable.favorite_on)
                favoriteContent?.storefavoritesong(currentSongHelper?.songID?.toInt(),
                        currentSongHelper?.songArtist, currentSongHelper?.songTitle, currentSongHelper?.songPath)
                Toast.makeText(myactivity, "Added to favorites", Toast.LENGTH_SHORT).show()
            }
        })




        shufflebutton?.setOnClickListener({
            var editorShuffle = myactivity?.getSharedPreferences(staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop = myactivity?.getSharedPreferences(staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()
            if (currentSongHelper?.isshuffle as Boolean) {
                shufflebutton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                currentSongHelper?.isshuffle = false
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            } else {
                currentSongHelper?.isshuffle = true
                currentSongHelper?.isloop = false
                shufflebutton?.setBackgroundResource(R.drawable.shuffle_icon)
                loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("feature", true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            }

        })

        loopbutton?.setOnClickListener({
            var editorShuffle = myactivity?.getSharedPreferences(staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop = myactivity?.getSharedPreferences(staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()

            if (currentSongHelper?.isloop as Boolean) {
                currentSongHelper?.isloop = false
                loop_button?.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            } else {
                currentSongHelper?.isloop = true
                currentSongHelper?.isshuffle = false
                loop_button?.setBackgroundResource(R.drawable.loop_icon)
                shufflebutton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", true)
                editorLoop?.apply()
            }
        })
        nextbutton?.setOnClickListener({
            //currentSongHelper?.isplaying = true
            if (currentposition == (fetchsongs?.size!! - 1)) {
                Toast.makeText(myactivity, "Last Song", Toast.LENGTH_SHORT).show()
            } else {
                if (currentSongHelper?.isshuffle as Boolean) {
                    playnext(2)
                } else {
                    playnext(1)
                }
            }
        })
        previousbutton?.setOnClickListener({
            currentSongHelper?.isplaying = true
            if (currentposition == 0) {
                Toast.makeText(myactivity, "No Previous", Toast.LENGTH_SHORT).show()
            } else {
                if (currentSongHelper?.isloop as Boolean) {
                    loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)
                } else {
                    playprevious()
                }
            }
        })
        playpausebutton?.setOnClickListener({
            if (mediaPlayer?.isPlaying as Boolean) {
                mediaPlayer?.pause()
                currentSongHelper?.isplaying = false
                playpausebutton?.setBackgroundResource(R.drawable.play_icon)
            } else {
                mediaPlayer?.start()
                currentSongHelper?.isplaying = true
                playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
    }


    fun playnext(check: Int) {
        if (check == 1) {
            currentposition += 1
        } else if (check == 2) {
            var random = Random()
            var randomposition = random.nextInt(fetchsongs?.size?.plus(1) as Int)
            currentposition = randomposition
            currentSongHelper?.isloop = false
        }
        var nextsong = fetchsongs?.get(currentposition)
        currentSongHelper?.currentPosition = currentposition
        currentSongHelper?.songPath = nextsong?.songData
        currentSongHelper?.songID = nextsong?.songID as Long
        currentSongHelper?.songTitle = nextsong?.songTitle
        currentSongHelper?.songArtist = nextsong?.artist
        updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)
        mediaPlayer?.reset()
        try {
            mediaPlayer?.setDataSource(myactivity, Uri.parse(currentSongHelper?.songPath))
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            processInformation(mediaPlayer as MediaPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (favoriteContent?.checkidexists(currentSongHelper?.songID?.toInt() as Int) as Boolean) {
            fab?.setBackgroundResource(R.drawable.favorite_on)
        } else {
            fab?.setBackgroundResource(R.drawable.favorite_off)
        }
    }

    fun playprevious() {
        currentposition -= 1
        currentSongHelper?.isloop = false
        var nextsong = fetchsongs?.get(currentposition)
        currentSongHelper?.currentPosition = currentposition
        currentSongHelper?.songPath = nextsong?.songData
        currentSongHelper?.songID = nextsong?.songID as Long
        currentSongHelper?.songTitle = nextsong?.songTitle
        currentSongHelper?.songArtist = nextsong?.artist
        updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)

        mediaPlayer?.reset()
        try {
            mediaPlayer?.setDataSource(myactivity, Uri.parse(currentSongHelper?.songPath))
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            processInformation(mediaPlayer as MediaPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (favoriteContent?.checkidexists(currentSongHelper?.songID?.toInt() as Int) as Boolean) {
            fab?.setBackgroundResource(R.drawable.favorite_on)
        } else {
            fab?.setBackgroundResource(R.drawable.favorite_off)
        }

    }

    fun onsongComplete() {
        if (currentSongHelper?.isshuffle as Boolean) {
            playnext(2)
            currentSongHelper?.isplaying = true
        } else {
            if (currentSongHelper?.isloop as Boolean) {
                currentSongHelper?.isplaying = true
                var nextsong = fetchsongs?.get(currentposition)
                currentSongHelper?.currentPosition = currentposition
                currentSongHelper?.songPath = nextsong?.songData
                currentSongHelper?.songID = nextsong?.songID as Long
                currentSongHelper?.songTitle = nextsong?.songTitle
                currentSongHelper?.songArtist = nextsong?.artist
                updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)

                mediaPlayer?.reset()
                try {
                    mediaPlayer?.setDataSource(myactivity, Uri.parse(currentSongHelper?.songPath))
                    mediaPlayer?.prepare()
                    mediaPlayer?.start()
                    processInformation(mediaPlayer as MediaPlayer)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } else {
                playnext(1)
                currentSongHelper?.isplaying = true
            }
        }
        if (favoriteContent?.checkidexists(currentSongHelper?.songID?.toInt() as Int) as Boolean) {
            fab?.setBackgroundResource(R.drawable.favorite_on)
        } else {
            fab?.setBackgroundResource(R.drawable.favorite_off)
        }
    }

    fun updateTextViews(title: String, artist: String) {
        songTitleview?.setText(title)
        songartistview?.setText(artist)

    }

    fun processInformation(mediaPlayer: MediaPlayer) {
        val finatime = mediaPlayer.duration
        val startTime = mediaPlayer?.currentPosition
        seekbar?.max = finatime
        starttimetext?.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(startTime?.toLong() as Long),
                TimeUnit.MILLISECONDS.toSeconds(startTime?.toLong() as Long) - TimeUnit.MILLISECONDS
                        .toSeconds(TimeUnit.MILLISECONDS
                                .toMinutes(startTime?.toLong() as Long))))

        endtimetext?.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(finatime?.toLong() as Long),
                TimeUnit.MILLISECONDS.toSeconds(finatime?.toLong() as Long) - TimeUnit.MILLISECONDS
                        .toSeconds(TimeUnit.MILLISECONDS
                                .toMinutes(finatime?.toLong() as Long))))
        seekbar?.setProgress(startTime)
        Handler().postDelayed(updatesongtime, 1000)
    }
}