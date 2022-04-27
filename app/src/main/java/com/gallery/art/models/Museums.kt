package com.gallery.art.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.collections.HashMap

@Parcelize
class Museums(
    var museumName: String,
    var museumDesc: String,
    var museumImage: String,
    var arts: HashMap<String, Arts>,
    var city: String,
    var uID: String,
    var latitude: Double,
    var longitude: Double
) : Parcelable {
    constructor() : this(
        "",
        "",
        "",
        HashMap<String, Arts>(),
        "",
        "",
        0.0,
        0.0
    )
}
