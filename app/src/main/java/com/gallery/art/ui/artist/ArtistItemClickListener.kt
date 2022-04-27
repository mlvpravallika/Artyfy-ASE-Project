package com.gallery.art.ui.artist

import com.gallery.art.models.Artist
import com.gallery.art.models.Museums

interface ArtistItemClickListener {
    fun onArtistClick(artist: Artist)
}