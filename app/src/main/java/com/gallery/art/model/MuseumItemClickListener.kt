package com.gallery.art.model

import com.gallery.art.models.Museums

interface MuseumItemClickListener {
    fun onMuseumClick(museums: Museums)
}