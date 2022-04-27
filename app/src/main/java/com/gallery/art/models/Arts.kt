package com.gallery.art.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Arts(
    var artId: String,
    var artName: String,
    var artUrl: String,
    var price: String,
    var artist: String,
    var sell: Boolean = false
): Parcelable {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        false
    )
}