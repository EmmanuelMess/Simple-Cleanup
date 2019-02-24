package com.emmanuelmess.simplecleanup

import android.os.Environment
import android.os.StatFs
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

    fun getInternalDirectory(): File {
        return Environment.getExternalStorageDirectory().absolutePath.trimEnd('/').toFile()
    }

    val availableSpaceInternalPercentage: Float get() {
        return getAvailableInternalMemorySize().toFloat() / getTotalInternalMemorySize().toFloat() * 100
    }

    private fun getAvailableInternalMemorySize(): Long {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        return availableBlocks * blockSize
    }

    private fun getTotalInternalMemorySize(): Long {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        return totalBlocks * blockSize
    }
}


