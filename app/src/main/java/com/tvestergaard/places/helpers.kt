package com.tvestergaard.places

import android.os.Handler
import android.os.Looper

// https://github.com/WindSekirun/RichUtilsKt/blob/master/RichUtils/src/main/java/pyxis/uzuki/live/richutilskt/utils/RThread.kt
fun runOnUiThread(action: () -> Unit) = Handler(Looper.getMainLooper()).post(Runnable(action))
