package com.tvestergaard.places

import android.content.Context
import android.location.Geocoder
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.tvestergaard.places.transport.BackendCommunicator
import kotlin.math.round

// https://githusb.com/WindSekirun/RichUtilsKt/blob/master/RichUtils/src/main/java/pyxis/uzuki/live/richutilskt/utils/RThread.kt
fun runOnUiThread(action: () -> Unit) = Handler(Looper.getMainLooper()).post(Runnable(action))

fun ImageView.glide(url: String) {
    Glide.with(context)
        .load("${BackendCommunicator.IMG_ROOT}/$url")
        .into(this)
}

fun Context.reverseGeocode(latitude: Double, longitude: Double): String {
    val geoCoder = Geocoder(this)
    val addresses = geoCoder.getFromLocation(latitude, longitude, 1)
    if (addresses != null && addresses.size > 0) {
        val address = addresses[0]
        val sb = StringBuilder()
        if (address.locality != null)
            sb.append(address.locality).append(", ")
        sb.append(address.countryName)
        return sb.toString()
    }

    return "Could not locate..."
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}