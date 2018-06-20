package com.example.ajayveersingh.musicplayer


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_mainscreen_fragment.*

class favrouites_fragment : Fragment() {

    var myactivity: Activity? = null
    var getSongslist: ArrayList<Songs>? = null
    var arraylist: ArrayList<Songs> = arrayListOf()
    var newarray: ArrayList<Songs> = arrayListOf()
    var nowPlayingBottomBar: RelativeLayout? = null
    var playpausebutton: ImageButton? = null
    var songtitle: TextView? = null
    var trackposition: Int = 0
    var noFavorites: TextView? = null
    var recyclerview: RecyclerView? = null


    object Statified {
        var mediaplayer: MediaPlayer? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.fragment_favrouites_fragment, container, false)

        noFavorites = view?.findViewById(R.id.nofavorites)
        nowPlayingBottomBar = view?.findViewById(R.id.fav_bottom_bar)
        songtitle = view?.findViewById(R.id.current_song)
        playpausebutton = view?.findViewById(R.id.play_pause_button)
        recyclerview = view?.findViewById(R.id.favorite_recycler)
        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myactivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myactivity = activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getSongslist = getSongsfromPhone()
        if (getSongslist == null) {
            recyclerview?.visibility = View.INVISIBLE
            noFavorites?.visibility = View.VISIBLE
        } else {
            var favoriteadapter = favoriteadapter(getSongslist as ArrayList<Songs>, myactivity as Context)
            recyclerview?.layoutManager = LinearLayoutManager(activity)
            recyclerview?.itemAnimator = DefaultItemAnimator()
            recyclerview?.adapter = favoriteadapter
            recyclerview?.setHasFixedSize(true)
        }
        bottombar_setup()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
    }

    fun getSongsfromPhone(): ArrayList<Songs>? {

        var contentresolver = myactivity?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var cursorSong = contentresolver?.query(songUri, null, null, null, null)
        if (cursorSong != null && cursorSong.moveToFirst()) {
            val songID = cursorSong.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = cursorSong.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = cursorSong.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = cursorSong.getColumnIndex(MediaStore.Audio.Media.DATA)
            val songDate = cursorSong.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            do {
                var currentId = cursorSong.getLong(songID)
                var currentTitle = cursorSong.getString(songTitle)
                var currentArtist = cursorSong.getString(songArtist)
                var currentData = cursorSong.getString(songData)
                var currentDate = cursorSong.getLong(songDate)
                if (songplaying_fragment.Statified.favoriteContent?.checkidexists(currentId.toInt()) as Boolean) {
                    arraylist?.add(Songs(currentId, currentTitle, currentArtist, currentData, currentDate))
                } else {
                    continue
                }
            } while (cursorSong.moveToNext())
        }
        return arraylist
    }

    fun bottombar_setup() {
        try {
            bottomBarCLickhandler()
            songtitle?.setText(songplaying_fragment.Statified.currentSongHelper?.songTitle)
            songplaying_fragment.Statified.mediaPlayer?.setOnCompletionListener({
                songtitle?.setText(songplaying_fragment.Statified.currentSongHelper?.songTitle)
                songplaying_fragment.Staticated.onsongComplete()
            })
            if (songplaying_fragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                nowPlayingBottomBar?.visibility = View.VISIBLE
            } else {
                nowPlayingBottomBar?.visibility = View.INVISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun bottomBarCLickhandler() {
        nowPlayingBottomBar?.setOnClickListener({
            Statified.mediaplayer = songplaying_fragment.Statified.mediaPlayer
            var args = Bundle()
            val songplayingfragment = songplaying_fragment()
            args.putString("songArtist", songplaying_fragment.Statified.currentSongHelper?.songArtist)
            args.putString("path", songplaying_fragment.Statified.currentSongHelper?.songPath)
            args.putString("songTitle", songplaying_fragment.Statified.currentSongHelper?.songTitle)
            args.putInt("songId", songplaying_fragment.Statified.currentSongHelper?.songID?.toInt() as Int)
            args.putInt("songPosition", songplaying_fragment.Statified.currentSongHelper?.currentPosition?.toInt() as Int)
            args.putParcelableArrayList("songData", songplaying_fragment.Statified.fetchsongs)
            args.putString("favBottomBar", "success")
            songplayingfragment.arguments = args

            fragmentManager?.beginTransaction()?.replace(R.id.fragment, songplayingfragment)
                    ?.commitNow()
        })

        playpausebutton?.setOnClickListener({
            if (songplaying_fragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                songplaying_fragment.Statified.mediaPlayer?.pause()
                trackposition = songplaying_fragment.Statified.mediaPlayer?.currentPosition as Int
                songplaying_fragment.Statified.currentSongHelper?.isplaying = false

                playpausebutton?.setBackgroundResource(R.drawable.play_icon)
            } else {
                songplaying_fragment.Statified?.mediaPlayer?.seekTo(trackposition?.toInt() as Int)
                songplaying_fragment.Statified.mediaPlayer?.start()
                songplaying_fragment.Statified.currentSongHelper?.isplaying = true
                playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
    }
}