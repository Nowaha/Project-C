package xyz.nowaha.chengetawildlife.util.extensions

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions

fun MarkerOptions.iconBasedOnType(type: String) {
    icon(BitmapDescriptorFactory.defaultMarker(when (type) {
        "animal" -> 140f
        "unknown" -> BitmapDescriptorFactory.HUE_ORANGE
        else -> BitmapDescriptorFactory.HUE_RED
    }))
}