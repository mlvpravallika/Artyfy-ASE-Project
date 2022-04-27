package com.gallery.art.ui.artist.manage

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.gallery.art.databinding.LayoutArtItemBinding
import com.gallery.art.models.Arts

class ArtistArtCollectionAdapter(
    val context: Context,
    private var values: MutableList<Arts>,
    private val artItemClickListener: ArtItemClickListener) : RecyclerView.Adapter<ArtistArtCollectionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutArtItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(values[position])
    }

    override fun getItemCount(): Int = values.size

    fun updateAdapter(userList: MutableList<Arts>) {
        this.values = userList
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: LayoutArtItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Arts) {
            binding.artName.text = item.artName
            binding.imageView.load(item.artUrl) {
                crossfade(true)
            }
            binding.museumDetailLayout.setOnClickListener {
                artItemClickListener.onArtClick(item)
            }
        }
    }

    interface ArtItemClickListener {
        fun onArtClick(arts: Arts)
    }
}