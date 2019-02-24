package com.emmanuelmess.simplecleanup.extensions

import android.os.storage.StorageManager
import android.provider.Settings
import java.io.File

/**
 * Return the number of available bytes until the given path is considered
 * running low on storage.
 */
fun StorageManager.getStorageFullBytes(path: File): Long {
    val f = StorageManager::class.java.getMethod("getStorageFullBytes", File::class.java)
    return f.invoke(this, path) as Long
}

/**
 * Return the number of available bytes at which the given path is
 * considered running low on storage.
 */
fun StorageManager.getStorageLowBytes(path: File): Long {
    val f = StorageManager::class.java.getMethod("getStorageLowBytes", File::class.java)
    return f.invoke(this, path) as Long
}

