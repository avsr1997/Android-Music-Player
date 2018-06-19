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
import android.view.*
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView

class mainscreen_fragment : Fragment() {
    var arraylist: ArrayList<Songs> = arrayListOf()

    var nowPlayingBottomBar: RelativeLayout? = null
    var playpausebutton: ImageButton? = null
    var songtitle: TextView? = null
    var visibleLayout: RelativeLayout? = null
    var noSongs: RelativeLayout? = null
    var recyclerView: RecyclerView? = null
    var myactivity: Activity? = null
    var trackposition: Int = 0
    var mainscreenadapter: mainscreen_adapter? = null

    object yoyo {
        var mediaplayer: MediaPlayer? = null
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_mainscreen_fragment, container, false)
        visibleLayout = view?.findViewById(R.id.main_content)
        noSongs = view?.findViewById(R.id.no_songs)
        nowPlayingBottomBar = view?.findViewById(R.id.bottom_bar1)
        songtitle = view?.findViewById(R.id.current_song)
        playpausebutton = view?.findViewById(R.id.play_pause_button)
        recyclerView = view?.findViewById(R.id.all_songs_view)

        return view
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        var songlist: ArrayList<Songs> = getSongsfromPhone()
        if (songlist.isEmpty()) {
            noSongs?.visibility = View.VISIBLE
        } else {
            mainscreenadapter = mainscreen_adapter(songlist as ArrayList<Songs>, myactivity as Context)
            mainscreenadapter?.notifyDataSetChanged()
            var linearLayoutManager = LinearLayoutManager(myactivity as Context)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            recyclerView?.layoutManager = linearLayoutManager
            recyclerView?.itemAnimator = DefaultItemAnimator()
            recyclerView?.adapter = mainscreenadapter
            recyclerView?.setHasFixedSize(true)
        }
        bottombar_setup()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myactivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myactivity = activity
    }

    fun getSongsfromPhone(): ArrayList<Songs> {

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
                arraylist.add(Songs(currentId, currentTitle, currentArtist, currentData, currentDate))
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
            yoyo.mediaplayer = songplaying_fragment.Statified.mediaPlayer
            var args = Bundle()
            val songplayingfragment = songplaying_fragment()
            args.putString("songArtist", songplaying_fragment.Statified.currentSongHelper?.songArtist)
            args.putString("path", songplaying_fragment.Statified.currentSongHelper?.songPath)
            args.putString("songTitle", songplaying_fragment.Statified.currentSongHelper?.songTitle)
            args.putInt("songId", songplaying_fragment.Statified.currentSongHelper?.songID?.toInt() as Int)
            args.putInt("songPosition", songplaying_fragment.Statified.currentSongHelper?.currentPosition?.toInt() as Int)
            args.putParcelableArrayList("songData", songplaying_fragment.Statified.fetchsongs)
            args.putString("mainBottomBar", "success")
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