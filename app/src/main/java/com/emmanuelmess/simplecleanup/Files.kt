package com.emmanuelmess.simplecleanup

import android.os.Environment
import com.emmanuelmess.simplecleanup.extensions.toFile
import java.io.File

object Files {
    val foldersToCheck = listOf(
        "/WhatsApp/Databases/",
        "/tencent/MicroMsg/"
    )

    fun processInternal(): List<File> {
        val internalStorageRootFile = getInternalDirectory()
        return foldersToCheck.map { path ->
            File(internalStorageRootFile, path)
        }.filter { file ->
            file.exists() //|| BuildConfig.DEBUG
        }
    }

    private fun getInternalDirectory(): File {
        return Environment.getExternalStorageDirectory().absolutePath.trimEnd('/').toFile()
    }
}


