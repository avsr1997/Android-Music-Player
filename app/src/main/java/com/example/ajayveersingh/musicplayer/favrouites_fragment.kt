package com.example.ajayveersingh.musicplayer


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
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
    var getsongslist: ArrayList<Songs>? = null
    var nofavorites: TextView? = null
    var bottombar: RelativeLayout? = null
    var playpausebutton: ImageButton? = null
    var songTitle: TextView? = null
    var recyclerView: RecyclerView? = null
    var trackposition = 0

    object statified {
        var mediaplayer: MediaPlayer? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favrouites_fragment, container, false)
        nofavorites = view?.findViewById(R.id.nofavorites)
        bottombar = view?.findViewById(R.id.bottom_bar)
        songTitle = view?.findViewById(R.id.songTitle)
        playpausebutton = view?.findViewById(R.id.play_pause_button)
        recyclerView = view?.findViewById(R.id.recycler_id)
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
        getsongslist = getSongsfromPhone()
        if (getsongslist == null) {
            recyclerView?.visibility = View.INVISIBLE
            nofavorites?.visibility = View.VISIBLE
        } else {
            var favoritsadapter: favoriteadapter = favoriteadapter(getsongslist as ArrayList<Songs>, myactivity as Context)
            recyclerView?.layoutManager = LinearLayoutManager(myactivity)
            recyclerView?.itemAnimator = DefaultItemAnimator()
            recyclerView?.adapter = favoritsadapter
            recyclerView?.hasFixedSize()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
    }

    fun getSongsfromPhone(): ArrayList<Songs> {
        var arraylist: ArrayList<Songs> = arrayListOf()
        var contentresolver = myactivity?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var cursorSong = contentresolver?.query(songUri, null, null, null, null)
        if (cursorSong != null && cursorSong.moveToFirst()) {
            val songID = cursorSong.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = cursorSong.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = cursorSong.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = cursorSong.getColumnIndex(MediaStore.Audio.Media.DATA)
            val songDate = cursorSong.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            while (cursorSong.moveToNext()) {
                var currentId = cursorSong.getLong(songID)
                var currentTitle = cursorSong.getString(songTitle)
                var currentArtist = cursorSong.getString(songArtist)
                var currentData = cursorSong.getString(songData)
                var currentDate = cursorSong.getLong(songDate)
                arraylist.add(Songs(currentId, currentTitle, currentArtist, currentData, currentDate))
            }
        }
        return arraylist
    }

    fun bottombar_setup() {
        bottombar_clickhandler()
        try {
            songTitle?.setText(songplaying_fragment.static.myhelper?.nowsongtitle)
            songplaying_fragment.static.mediaPlayer?.setOnCompletionListener({
                songTitle?.setText(songplaying_fragment.static.myhelper?.nowsongtitle)
                songplaying_fragment.stat.onsongComplete()
            })
            if (songplaying_fragment.static.mediaPlayer?.isPlaying as Boolean) {
                bottombar?.visibility = View.VISIBLE
            } else {
                bottombar?.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun bottombar_clickhandler() {
        statified.mediaplayer = songplaying_fragment.static.mediaPlayer
        var songplayingfragment = songplaying_fragment()
        bottombar?.setOnClickListener({
            var args = Bundle()
            args.putString("songArtist", songplaying_fragment.static.myhelper?.nowartist)
            args.putString("path", songplaying_fragment.static.myhelper?.nowartist)
            args.putString("songTitle", songplaying_fragment.static.myhelper?.nowartist)
            args.putInt("songId", songplaying_fragment.static.myhelper?.nowsongID as Int)
            args.putInt("songPosition", songplaying_fragment.static.myhelper?.nowposition)
            args.putParcelableArrayList("songData", songplaying_fragment.static.songDetails)
            args.putString("favBottomBar", "Success")
            fragmentManager?.beginTransaction()
                    ?.replace(R.id.fragment, songplayingfragment)?.addToBackStack("songplayingfragment")
                    ?.commit()
        })
        playpausebutton?.setOnClickListener({
            if (songplaying_fragment.static.myhelper?.isplaying as Boolean) {
                songplaying_fragment.static.mediaPlayer?.pause()
                trackposition = songplaying_fragment.static.mediaPlayer?.currentPosition as Int
                playpausebutton?.setBackgroundResource(R.drawable.play_icon)
            } else {
                songplaying_fragment.static.mediaPlayer?.seekTo(trackposition)
                songplaying_fragment.static.mediaPlayer?.start()
                playpausebutton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
    }
}