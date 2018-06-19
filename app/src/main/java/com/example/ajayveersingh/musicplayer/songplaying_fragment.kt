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

    object Statified {
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
    }

    object Staticated {
        val MY_PREFS_SHUFFLE = "Shuffle Feature"
        val MY_PREFS_LOOP = "loop feature"

        fun playnext(check: Int) {
            if (check == 1) {
                Statified.currentposition += 1
            } else if (check == 2) {
                var random = Random()
                var randomposition = random.nextInt(Statified.fetchsongs?.size?.plus(1) as Int)
                Statified.currentposition = randomposition
                Statified.currentSongHelper?.isloop = false
            }
            var nextsong = Statified.fetchsongs?.get(Statified.currentposition)
            Statified.currentSongHelper?.currentPosition = Statified.currentposition
            Statified.currentSongHelper?.songPath = nextsong?.songData
            Statified.currentSongHelper?.songID = nextsong?.songID as Long
            Statified.currentSongHelper?.songTitle = nextsong?.songTitle
            Statified.currentSongHelper?.songArtist = nextsong?.artist
            updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)
            Statified.mediaPlayer?.reset()
            try {
                Statified.mediaPlayer?.setDataSource(Statified.myactivity, Uri.parse(Statified.currentSongHelper?.songPath))
                Statified.mediaPlayer?.prepare()
                Statified.mediaPlayer?.start()
                processInformation(Statified.mediaPlayer as MediaPlayer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (Statified.favoriteContent?.checkidexists(Statified.currentSongHelper?.songID?.toInt() as Int) as Boolean) {
                Statified.fab?.setBackgroundResource(R.drawable.favorite_on)
            } else {
                Statified.fab?.setBackgroundResource(R.drawable.favorite_off)
            }
        }

        fun updateTextViews(title: String, artist: String) {
            Statified.songTitleview?.setText(title)
            Statified.songartistview?.setText(artist)

        }

        fun processInformation(mediaPlayer: MediaPlayer) {
            val finatime = mediaPlayer.duration
            val startTime = mediaPlayer?.currentPosition
            Statified.seekbar?.max = finatime
            Statified.starttimetext?.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(startTime?.toLong() as Long),
                    TimeUnit.MILLISECONDS.toSeconds(startTime?.toLong() as Long) - TimeUnit.MILLISECONDS
                            .toSeconds(TimeUnit.MILLISECONDS
                                    .toMinutes(startTime?.toLong() as Long))))

            Statified.endtimetext?.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(finatime?.toLong() as Long),
                    TimeUnit.MILLISECONDS.toSeconds(finatime?.toLong() as Long) - TimeUnit.MILLISECONDS
                            .toSeconds(TimeUnit.MILLISECONDS
                                    .toMinutes(finatime?.toLong() as Long))))
            Statified.seekbar?.setProgress(startTime)
            Handler().postDelayed(Statified.updatesongtime, 1000)
        }

        fun onsongComplete() {
            if (Statified.currentSongHelper?.isshuffle as Boolean) {
                playnext(2)
                Statified.currentSongHelper?.isplaying = true
            } else {
                if (Statified.currentSongHelper?.isloop as Boolean) {
                    Statified.currentSongHelper?.isplaying = true
                    var nextsong = Statified.fetchsongs?.get(Statified.currentposition)
                    Statified.currentSongHelper?.currentPosition = Statified.currentposition
                    Statified.currentSongHelper?.songPath = nextsong?.songData
                    Statified.currentSongHelper?.songID = nextsong?.songID as Long
                    Statified.currentSongHelper?.songTitle = nextsong?.songTitle
                    Statified.currentSongHelper?.songArtist = nextsong?.artist
                    updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)

                    Statified.mediaPlayer?.reset()
                    try {
                        Statified.mediaPlayer?.setDataSource(Statified.myactivity, Uri.parse(Statified.currentSongHelper?.songPath))
                        Statified.mediaPlayer?.prepare()
                        Statified.mediaPlayer?.start()
                        processInformation(Statified.mediaPlayer as MediaPlayer)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                } else {
                    playnext(1)
                    Statified.currentSongHelper?.isplaying = true
                }
            }
            if (Statified.favoriteContent?.checkidexists(Statified.currentSongHelper?.songID?.toInt() as Int) as Boolean) {
                Statified.fab?.setBackgroundResource(R.drawable.favorite_on)
            } else {
                Statified.fab?.setBackgroundResource(R.drawable.favorite_off)
            }
        }


    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_songplaying_fragment, container, false)
        Statified.seekbar = view?.findViewById(R.id.seekbar)
        Statified.starttimetext = view?.findViewById(R.id.start_time)
        Statified.endtimetext = view?.findViewById(R.id.end_time)
        Statified.playpausebutton = view?.findViewById(R.id.playpause_button)
        Statified.nextbutton = view?.findViewById(R.id.next_button)
        Statified.previousbutton = view?.findViewById(R.id.previousbutton)
        Statified.loopbutton = view?.findViewById(R.id.loop_button)
        Statified.shufflebutton = view?.findViewById(R.id.shuffle_button)
        Statified.songTitleview = view?.findViewById(R.id.songTitle)
        Statified.songartistview = view?.findViewById(R.id.songArtist)
        Statified.glview = view?.findViewById(R.id.visualizer_view)
        Statified.fab = view?.findViewById(R.id.favorites)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Statified.audioVisualization = Statified.glview as AudioVisualization
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Statified.myactivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        Statified.myactivity = activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Statified.favoriteContent = favorite_database(Statified.myactivity)

        Statified.currentSongHelper = CurrentSongHelper()
        Statified.currentSongHelper?.isplaying = true
        Statified.currentSongHelper?.isloop = false
        Statified.currentSongHelper?.isshuffle = false
        var path: String? = null
        var _songTitle: String? = null
        var _songArtist: String? = null
        var songID: Long = 0

        try {
            path = arguments?.getString("path")
            _songTitle = arguments?.getString("songTitle")
            _songArtist = arguments?.getString("songArtist")
            songID = arguments?.getInt("songId")!!.toLong()
            Statified.currentposition = arguments?.getInt("songPosition") as Int
            Statified.fetchsongs = arguments?.getParcelableArrayList("songData")

            Statified.currentSongHelper?.currentPosition = Statified.currentposition
            Statified.currentSongHelper?.songPath = path
            Statified.currentSongHelper?.songID = songID
            Statified.currentSongHelper?.songTitle = _songTitle
            Statified.currentSongHelper?.songArtist = _songArtist
            Staticated.updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        var fromfavButton = arguments?.get("favBottomBar") as? String
        var from_main_button = arguments?.get("mainBottomBar") as? String
        if (fromfavButton.equals("favBottomBar")) {
            Statified.mediaPlayer = favrouites_fragment.Statified.mediaplayer
        } else if (from_main_button.equals("mainBottomBar")) {
            Statified.mediaPlayer = mainscreen_fragment.yoyo.mediaplayer
        } else {
            Statified.mediaPlayer = MediaPlayer()
            Statified.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                Statified.mediaPlayer?.setDataSource(Statified.myactivity, Uri.parse(path))
                Statified.mediaPlayer?.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Statified.mediaPlayer?.start()
        }
        Staticated.processInformation(Statified.mediaPlayer as MediaPlayer)
        if (Statified.currentSongHelper?.isplaying as Boolean) {
            Statified.playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            Statified.playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
        }
        Statified.mediaPlayer?.setOnCompletionListener({
            Staticated.onsongComplete()
        })

        clickhandler()
        var visualizerHandler = DbmHandler.Factory.newVisualizerHandler(Statified.myactivity as Context, 0)
        Statified.audioVisualization?.linkTo(visualizerHandler)

        var prefsForShuffle = Statified.myactivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature", false)
        if (isShuffleAllowed as Boolean) {
            Statified.currentSongHelper?.isshuffle = true
            Statified.currentSongHelper?.isloop = false
            Statified.shufflebutton?.setBackgroundResource(R.drawable.shuffle_icon)
            Statified.loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)

        } else {
            Statified.currentSongHelper?.isshuffle = false
            Statified.shufflebutton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }

        var prefsForLoop = Statified.myactivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForShuffle?.getBoolean("feature", false)
        if (isShuffleAllowed as Boolean) {
            Statified.currentSongHelper?.isshuffle = false
            Statified.currentSongHelper?.isloop = true
            Statified.shufflebutton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            Statified.loopbutton?.setBackgroundResource(R.drawable.loop_icon)

        } else {
            Statified.currentSongHelper?.isloop = false
            Statified.loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)
        }
        if (Statified.favoriteContent?.checkidexists(Statified.currentSongHelper?.songID?.toInt() as Int) as Boolean) {
            Statified.fab?.setBackgroundResource(R.drawable.favorite_on)
        } else {
            Statified.fab?.setBackgroundResource(R.drawable.favorite_off)
        }
    }


    override fun onPause() {
        super.onPause()
        Statified.audioVisualization?.onPause()
    }

    override fun onResume() {
        super.onResume()
        Statified.audioVisualization?.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        Statified.audioVisualization?.release()
    }


    fun clickhandler() {
        Statified.fab?.setOnClickListener({
            if (Statified.favoriteContent?.checkidexists(Statified.currentSongHelper?.songID?.toInt() as Int) as Boolean) {
                Statified.fab?.setBackgroundResource(R.drawable.favorite_off)
                Statified.favoriteContent?.deletesong_database(Statified.currentSongHelper?.songID?.toInt() as Int)
                Toast.makeText(Statified.myactivity, "Removed from favorites", Toast.LENGTH_SHORT).show()
            } else {
                Statified.fab?.setBackgroundResource(R.drawable.favorite_on)
                Statified.favoriteContent?.storefavoritesong(Statified.currentSongHelper?.songID?.toInt(),
                        Statified.currentSongHelper?.songArtist, Statified.currentSongHelper?.songTitle, Statified.currentSongHelper?.songPath)
                Toast.makeText(Statified.myactivity, "Added to favorites", Toast.LENGTH_SHORT).show()
            }
        })




        Statified.shufflebutton?.setOnClickListener({
            var editorShuffle = Statified.myactivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop = Statified.myactivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()
            if (Statified.currentSongHelper?.isshuffle as Boolean) {
                Statified.shufflebutton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                Statified.currentSongHelper?.isshuffle = false
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            } else {
                Statified.currentSongHelper?.isshuffle = true
                Statified.currentSongHelper?.isloop = false
                Statified.shufflebutton?.setBackgroundResource(R.drawable.shuffle_icon)
                Statified.loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("feature", true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            }

        })

        Statified.loopbutton?.setOnClickListener({
            var editorShuffle = Statified.myactivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop = Statified.myactivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()

            if (Statified.currentSongHelper?.isloop as Boolean) {
                Statified.currentSongHelper?.isloop = false
                loop_button?.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            } else {
                Statified.currentSongHelper?.isloop = true
                Statified.currentSongHelper?.isshuffle = false
                loop_button?.setBackgroundResource(R.drawable.loop_icon)
                Statified.shufflebutton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", true)
                editorLoop?.apply()
            }
        })
        Statified.nextbutton?.setOnClickListener({
            //currentSongHelper?.isplaying = true
            if (Statified.currentposition == (Statified.fetchsongs?.size!! - 1)) {
                Toast.makeText(Statified.myactivity, "Last Song", Toast.LENGTH_SHORT).show()
            } else {
                if (Statified.currentSongHelper?.isshuffle as Boolean) {
                    Staticated.playnext(2)
                } else {
                    Staticated.playnext(1)
                }
            }
        })
        previousbutton?.setOnClickListener({
            Statified.currentSongHelper?.isplaying = true
            if (Statified.currentposition == 0) {
                Toast.makeText(Statified.myactivity, "No Previous", Toast.LENGTH_SHORT).show()
            } else {
                if (Statified.currentSongHelper?.isloop as Boolean) {
                    Statified.loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)
                } else {
                    playprevious()
                }
            }
        })
        Statified.playpausebutton?.setOnClickListener({
            if (Statified.mediaPlayer?.isPlaying as Boolean) {
                Statified.mediaPlayer?.pause()
                Statified.currentSongHelper?.isplaying = false
                Statified.playpausebutton?.setBackgroundResource(R.drawable.play_icon)
            } else {
                Statified.mediaPlayer?.start()
                Statified.currentSongHelper?.isplaying = true
                Statified.playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
    }


    fun playprevious() {
        Statified.currentposition -= 1
        Statified.currentSongHelper?.isloop = false
        var nextsong = Statified.fetchsongs?.get(Statified.currentposition)
        Statified.currentSongHelper?.currentPosition = Statified.currentposition
        Statified.currentSongHelper?.songPath = nextsong?.songData
        Statified.currentSongHelper?.songID = nextsong?.songID as Long
        Statified.currentSongHelper?.songTitle = nextsong?.songTitle
        Statified.currentSongHelper?.songArtist = nextsong?.artist
        Staticated.updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)

        Statified.mediaPlayer?.reset()
        try {
            Statified.mediaPlayer?.setDataSource(Statified.myactivity, Uri.parse(Statified.currentSongHelper?.songPath))
            Statified.mediaPlayer?.prepare()
            Statified.mediaPlayer?.start()
            Staticated.processInformation(Statified.mediaPlayer as MediaPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (Statified.favoriteContent?.checkidexists(Statified.currentSongHelper?.songID?.toInt() as Int) as Boolean) {
            Statified.fab?.setBackgroundResource(R.drawable.favorite_on)
        } else {
            Statified.fab?.setBackgroundResource(R.drawable.favorite_off)
        }

    }


}