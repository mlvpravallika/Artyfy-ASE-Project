package com.gallery.art.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.collections.HashMap

@Parcelize
class Artist(
    var artistName: String,
    var artistImage: String,
    var arts: HashMap<String, Arts>,
    var city: String,
    var uID: String
) : Parcelable {
    constructor() : this(
        "",
        "",
        HashMap<String, Arts>(),
        "",
        ""
    )
}
