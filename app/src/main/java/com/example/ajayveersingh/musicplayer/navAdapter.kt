package com.example.ajayveersingh.musicplayer

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

class navAdapter(_contentList: ArrayList<String>, _getImages: IntArray, _context:Context):RecyclerView.Adapter<navAdapter.navViewholder>()
{
    var contentList:ArrayList<String>?=null
    var getImages:IntArray?=null
    var context:Context?=null
    init
    {
        this.contentList=_contentList
        this.getImages=_getImages
        this.context=_context
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): navViewholder
    {
        var itemview=LayoutInflater.from(parent?.context).inflate(R.layout.recycler_view_layout,parent,false)
        var returnthis=navViewholder(itemview)
        return returnthis
    }

    override fun getItemCount(): Int
    {
        return contentList?.size as Int
    }

    override fun onBindViewHolder(holder: navViewholder, position: Int)
    {
        holder?.icon_get?.setBackgroundResource(getImages?.get(position) as Int)
        holder?.text_get?.setText(contentList?.get(position))
        holder?.contentholder?.setOnClickListener({
            if(position==0)
            {
                val mainscreenfragment=mainscreen_fragment()
                (context as MainActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment, mainscreenfragment).commit()
            }
            else if(position==1)
            {
                val favrouitesfragment=favrouites_fragment()
                (context as MainActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment, favrouitesfragment).commit()

            }
            else if(position==2)
            {
                val settingsfragment=settings_fragment()
                (context as MainActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment, settingsfragment).commit()

            }
            else if(position==3)
            {
                val aboutusfragment=aboutus_fragment()
                (context as MainActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment, aboutusfragment).commit()

            }
        })

    }

    class navViewholder(itemView: View?) : RecyclerView.ViewHolder(itemView)
    {
        var icon_get:ImageView?=null
        var text_get:TextView?=null
        var contentholder:RelativeLayout?=null
        init
        {
            icon_get=itemView?.findViewById(R.id.image1)
            text_get=itemView?.findViewById(R.id.tv1)
            contentholder=itemView?.findViewById(R.id.recycler_id)

        }
    }
}