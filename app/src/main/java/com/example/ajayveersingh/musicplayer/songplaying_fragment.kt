package com.example.ajayveersingh.musicplayer


import android.app.Activity
import android.content.Context
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
        shufflebutton?.setOnClickListener({
            if (currentSongHelper?.isshuffle as Boolean) {
                shufflebutton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                currentSongHelper?.isshuffle = false
            } else {
                currentSongHelper?.isshuffle = true
                currentSongHelper?.isloop = false
                shufflebutton?.setBackgroundResource(R.drawable.shuffle_icon)
                loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)
                currentSongHelper?.isshuffle = false
            }

        })

        loopbutton?.setOnClickListener({
            if (currentSongHelper?.isloop as Boolean) {
                currentSongHelper?.isloop = false
                loop_button?.setBackgroundResource(R.drawable.loop_white_icon)
            } else {
                currentSongHelper?.isloop = true
                currentSongHelper?.isshuffle = false
                loop_button?.setBackgroundResource(R.drawable.loop_icon)
                shufflebutton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            }
        })
        nextbutton?.setOnClickListener({
            currentSongHelper?.isplaying = true
            if (currentSongHelper?.isshuffle as Boolean) {
                playnext("PlayNextLikeNormalShuffle")
            } else {
                playnext("PlayNextNormal ")
            }
        })
        previousbutton?.setOnClickListener({
            currentSongHelper?.isplaying = true
            if (currentSongHelper?.isloop as Boolean) {
                loopbutton?.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playprevious()
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


    fun playnext(check: String) {
        if (check.equals("PlayNextNormal", true)) {
            currentposition += 1
        } else if (check.equals("PlayNextLikeNormalShuffle", true)) {
            var random = Random()
            var randomposition = random.nextInt(fetchsongs?.size?.plus(1) as Int)
            currentposition = randomposition
        }
        if (currentposition == fetchsongs?.size) {
            currentposition = 0
        }
        currentSongHelper?.isloop = false
        var nextsong = fetchsongs?.get(currentposition)
        currentSongHelper?.currentPosition = currentposition
        currentSongHelper?.songPath = nextsong?.songData
        currentSongHelper?.songID = (nextsong?.songID as Int).toLong()
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
    }

    fun playprevious() {
        currentposition -= 1
        if (currentposition == -1) {
            currentposition == 0
        }
        if (currentSongHelper?.isplaying as Boolean) {
            playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            playpausebutton?.setBackgroundResource(R.drawable.play_icon)
        }
        currentSongHelper?.isloop = false

        var nextsong = fetchsongs?.get(currentposition)
        currentSongHelper?.currentPosition = currentposition
        currentSongHelper?.songPath = nextsong?.songData
        currentSongHelper?.songID = (nextsong?.songID as Int).toLong()
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

    }

    fun onsongComplete() {
        if (currentSongHelper?.isshuffle as Boolean) {
            playnext("PlayNextLikeNormalShuffle")
            currentSongHelper?.isplaying = true
        } else {
            if (currentSongHelper?.isloop as Boolean) {
                currentSongHelper?.isplaying = true
                var nextsong = fetchsongs?.get(currentposition)
                currentSongHelper?.currentPosition = currentposition
                currentSongHelper?.songPath = nextsong?.songData
                currentSongHelper?.songID = (nextsong?.songID as Int).toLong()
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
                playnext("PlayNextNormal")
                currentSongHelper?.isplaying = true
            }
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