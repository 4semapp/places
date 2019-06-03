package com.tvestergaard.places

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat

/**
 * Checks if the application has all necessary permissions.
 */
private fun hasPermissionsInternal(context: Context, permissions: Array<String>): Boolean {
    for (permission in permissions)
        if (!isGranted(ContextCompat.checkSelfPermission(context, permission)))
            return false

    return true
}

fun Context.hasPermissions(permissions: Array<String>): Boolean {
    return hasPermissionsInternal(this, permissions)
}

fun Fragment.hasPermissions(permissions: Array<String>): Boolean {
    return hasPermissionsInternal(activity, permissions)
}

fun isGranted(code: Int): Boolean {
    return code == PackageManager.PERMISSION_GRANTED
}

fun isGranted(codes: IntArray): Boolean {
    if (codes.isEmpty())
        return false

    for (code in codes)
        if (code == PackageManager.PERMISSION_DENIED)
            return false

    return true
}