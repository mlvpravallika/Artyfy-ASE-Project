package com.gallery.art.ui.artist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.gallery.art.R
import com.gallery.art.models.Artist

class ArtistListAdapter(private val listData: MutableList<Artist?>,
                        private val clickListener: ArtistItemClickListener) :
    RecyclerView.Adapter<ArtistListAdapter.ArtistListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem =
            layoutInflater.inflate(R.layout.home_custom_gallery_cell, parent, false)
        return ArtistListViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ArtistListViewHolder, position: Int) {
        val myListData = listData[position]
        holder.textView.text = listData[position]?.artistName
        holder.relativeLayout.setOnClickListener {
            clickListener.onArtistClick(myListData!!)
        }
        holder.imageView.load(myListData?.artistImage) {
            crossfade(true)
        }
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    class ArtistListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.imageView)
        var textView: TextView = itemView.findViewById(R.id.textView)
        var relativeLayout: ConstraintLayout = itemView.findViewById(R.id.relativeLayout)
    }

}