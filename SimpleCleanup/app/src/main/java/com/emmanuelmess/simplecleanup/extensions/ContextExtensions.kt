package com.emmanuelmess.simplecleanup.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

fun Context.hasPermission(permId: String)
        = ContextCompat.checkSelfPermission(this, permId) == PackageManager.PERMISSION_GRANTED
