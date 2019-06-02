package com.tvestergaard.places

import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.tvestergaard.places.transport.BackendCommunicator

// https://githusb.com/WindSekirun/RichUtilsKt/blob/master/RichUtils/src/main/java/pyxis/uzuki/live/richutilskt/utils/RThread.kt
fun runOnUiThread(action: () -> Unit) = Handler(Looper.getMainLooper()).post(Runnable(action))

fun ImageView.glide(url: String) {
    Glide.with(context)
        .load("${BackendCommunicator.IMG_ROOT}/$url")
        .into(this)
}