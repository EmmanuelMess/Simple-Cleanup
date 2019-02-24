package com.emmanuelmess.simplecleanup.extensions

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

fun Context.hasPermission(permId: String)
        = ContextCompat.checkSelfPermission(this, permId) == PackageManager.PERMISSION_GRANTED

@ColorInt
fun Context.getColorCompat(@ColorRes color: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        getColor(color)
    } else {
        resources.getColor(color)
    }
}