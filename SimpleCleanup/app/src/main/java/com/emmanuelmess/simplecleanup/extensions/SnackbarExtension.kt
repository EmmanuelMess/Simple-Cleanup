package com.emmanuelmess.simplecleanup.extensions

import android.R.attr.textColor
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import com.google.android.material.snackbar.Snackbar

fun Snackbar.setColor(@ColorRes color: Int) {
    view.setBackgroundColor(context.getColorCompat(color))
}