package xyz.nowaha.chengetawildlife.extensions

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions

fun MarkerOptions.iconBasedOnType(type: String) {
    icon(BitmapDescriptorFactory.defaultMarker(when (type) {
        "animal" -> 135f
        "unknown" -> BitmapDescriptorFactory.HUE_ORANGE
        else -> BitmapDescriptorFactory.HUE_RED
    }))
}