package com.example.ajayveersingh.musicplayer

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import org.w3c.dom.Text

class mainscreen_adapter(_songDetails: ArrayList<Songs>, _context: Context) : RecyclerView.Adapter<mainscreen_adapter.MyViewHolder>() {
    var songDetails: ArrayList<Songs>? = null
    var mcontext: Context? = null

    init {
        this.songDetails = _songDetails
        this.mcontext = _context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemview = LayoutInflater.from(parent.context).inflate(R.layout.main_screen_recyclerview, parent, false)
        var returnthis = MyViewHolder(itemview)
        return returnthis


    }

    override fun getItemCount(): Int {
        if (songDetails == null) {
            return 0
        } else {
            return (songDetails as ArrayList<Songs>).size
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var songObject = songDetails?.get(position)
        holder.track_title?.text = songObject?.songTitle
        holder.track_artist?.text = songObject?.artist
        holder.contentHolder?.setOnClickListener({
            Toast.makeText(mcontext, songObject?.songTitle, Toast.LENGTH_SHORT).show()
        })
    }

    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var track_title: TextView? = null
        var track_artist: TextView? = null
        var contentHolder: RelativeLayout? = null

        init {
            track_title = itemView?.findViewById(R.id.track_title)
            track_artist = itemView?.findViewById(R.id.track_artist)
            contentHolder = itemView?.findViewById(R.id.contentholder)
        }
    }
}


