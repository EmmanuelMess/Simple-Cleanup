package com.emmanuelmess.simplecleanup

import androidx.annotation.ColorRes
import com.emmanuelmess.simplecleanup.extensions.setColor
import com.google.android.material.snackbar.Snackbar

class ResetableSnackbar(val snackbar: Snackbar) {
    fun setColor(@ColorRes color: Int) {
        snackbar.setColor(color)
    }

    fun resetColor() {
        snackbar.setColor(R.color.colorNormalSnackbar)
    }
}