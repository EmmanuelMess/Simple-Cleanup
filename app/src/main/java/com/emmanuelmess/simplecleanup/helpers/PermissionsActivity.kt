package com.emmanuelmess.simplecleanup.helpers

import android.annotation.SuppressLint
import android.app.Activity
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import com.emmanuelmess.simplecleanup.extensions.hasPermission

/**
 * Code authored by Tibor Kaputa for Simple-Commons (https://github.com/SimpleMobileTools)
 */
@SuppressLint("Registered")
open class PermissionsActivity: AppCompatActivity() {
    private val GENERIC_PERM_HANDLER = 100

    private var isAskingPermissions = false
    private var actionOnPermission: ((granted: Boolean) -> Unit)? = null

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        isAskingPermissions = false
        if (requestCode == GENERIC_PERM_HANDLER && grantResults.isNotEmpty()) {
            actionOnPermission?.invoke(grantResults[0] == 0)
        }
    }

    fun handlePermission(permissionId: String, callback: (granted: Boolean) -> Unit) {
        actionOnPermission = null
        if (hasPermission(permissionId)) {
            callback(true)
        } else {
            isAskingPermissions = true
            actionOnPermission = callback
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permissionId),
                GENERIC_PERM_HANDLER
            )
        }
    }
}