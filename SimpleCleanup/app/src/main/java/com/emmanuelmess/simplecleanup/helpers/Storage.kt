package com.emmanuelmess.simplecleanup.helpers

import android.content.Context
import android.os.storage.StorageManager
import com.emmanuelmess.simplecleanup.extensions.getStorageFullBytes
import com.emmanuelmess.simplecleanup.extensions.getStorageLowBytes
import java.io.File

/**
 * This simply uses the same heuristic as the OS uses for detecting low space
 *
 * @see [com.android.server.storage.DeviceStorageMonitorService.check]
 */
fun isStorageFragmenting(context: Context, file: File): Boolean {
    try {
        val storage = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val fullBytes = storage.getStorageFullBytes(file)
        val lowBytes = storage.getStorageLowBytes(file)
        val usableBytes = file.usableSpace

        return usableBytes <= fullBytes //full
                || usableBytes <= lowBytes//low
    } catch (e: ReflectiveOperationException) {
        return false
    }
}