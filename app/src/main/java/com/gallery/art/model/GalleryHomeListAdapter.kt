package com.gallery.art.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.gallery.art.R
import com.gallery.art.model.GalleryHomeListAdapter.GalleryListViewHolder
import com.gallery.art.models.Museums

class GalleryHomeListAdapter(private val listData: List<Museums>,
                             private val clickListener: MuseumItemClickListener) :
    RecyclerView.Adapter<GalleryListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem =
            layoutInflater.inflate(R.layout.home_custom_gallery_cell, parent, false)
        return GalleryListViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: GalleryListViewHolder, position: Int) {
        val myListData = listData[position]
        holder.textView.text = listData[position].museumName
        holder.relativeLayout.setOnClickListener {
            clickListener.onMuseumClick(myListData)
        }
        holder.imageView.load(myListData.museumImage) {
            crossfade(true)
        }
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    class GalleryListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.imageView)
        var textView: TextView = itemView.findViewById(R.id.textView)
        var relativeLayout: ConstraintLayout = itemView.findViewById(R.id.relativeLayout)
    }

}