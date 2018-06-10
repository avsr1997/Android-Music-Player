package com.example.ajayveersingh.musicplayer


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView

class mainscreen_fragment : Fragment() {

    var nowPlayingBottomBar:RelativeLayout?=null
    var playPauseButton:ImageButton?=null
    var songTitle:TextView?=null
    var visibleLayout:RelativeLayout?=null
    var noSongs:RelativeLayout?=null
    var recyclerView:RecyclerView?=null
    var myactivity:Activity?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val view=inflater!!.inflate(R.layout.fragment_mainscreen_fragment,container,false)
        visibleLayout=view?.findViewById(R.id.main_content)
        noSongs=view?.findViewById(R.id.no_songs)
        nowPlayingBottomBar=view?.findViewById(R.id.bottom_bar)
        songTitle=view?.findViewById(R.id.current_song)
        playPauseButton=view?.findViewById(R.id.play_pause_button)
        recyclerView=view?.findViewById(R.id.all_songs_view)

        return view
    }

    override fun onAttach(context: Context?)
    {
        super.onAttach(context)
        myactivity=context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myactivity=activity
    }

    fun getSongsfromPhone():ArrayList<Songs>
    {
        var arraylist:ArrayList<Songs> = arrayListOf()
        var contentresolver=myactivity?.contentResolver
        var songUri=MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var cursorSong=contentresolver?.query(songUri,null,null,null,null)
        if(cursorSong!=null && cursorSong.moveToFirst())
        {
            val songID=cursorSong.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle=cursorSong.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist=cursorSong.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData=cursorSong.getColumnIndex(MediaStore.Audio.Media.DATA)
            val songDate=cursorSong.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            while(cursorSong.moveToNext())
            {
                var currentId=cursorSong.getLong(songID)
                var currentTitle=cursorSong.getString(songTitle)
                var currentArtist=cursorSong.getString(songArtist)
                var currentData=cursorSong.getString(songData)
                var currentDate=cursorSong.getLong(songDate)
                arraylist.add(Songs(currentId,currentTitle,currentArtist,currentData,currentDate))
            }
        }
        return arraylist
    }
}
