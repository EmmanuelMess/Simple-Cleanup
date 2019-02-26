package com.emmanuelmess.simplecleanup

import android.os.Environment
import android.os.StatFs
import androidx.annotation.IntDef
import com.emmanuelmess.simplecleanup.FileMetadata.Companion.AMOUNT_OF_FILE_CATEGORIES
import com.emmanuelmess.simplecleanup.FileMetadata.Companion.CACHE
import com.emmanuelmess.simplecleanup.FileMetadata.Companion.IMPORTANT
import com.emmanuelmess.simplecleanup.FileMetadata.Companion.LAST_BACKUP
import com.emmanuelmess.simplecleanup.FileMetadata.Companion.LOW
import com.emmanuelmess.simplecleanup.FileMetadata.Companion.MEDIA
import com.emmanuelmess.simplecleanup.FileMetadata.Companion.NORMAL
import com.emmanuelmess.simplecleanup.FileMetadata.Companion.OLD_BACKUP
import com.emmanuelmess.simplecleanup.FileMetadata.Companion.OLD_MEDIA
import com.emmanuelmess.simplecleanup.FileMetadata.Companion.TRASH
import com.emmanuelmess.simplecleanup.extensions.toFile
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

data class FileCached(val file: File, val path: String, val size: Long, val metadata: FileMetadata)

data class FileMetadata(
    @ImportanceLevel val importanceLevel: Int,
    @FileCategory val fileCategory: Int
): Comparable<FileMetadata> {
    companion object {
        const val IMPORTANT = 4
        const val NORMAL = 2
        const val LOW = 1
        const val TRASH = 0

        @IntDef(IMPORTANT, NORMAL, LOW, TRASH)
        annotation class ImportanceLevel

        const val AMOUNT_OF_FILE_CATEGORIES = 5

        const val OLD_BACKUP = 0
        const val CACHE = 1
        const val LAST_BACKUP = 2
        const val OLD_MEDIA = 3
        const val MEDIA = 4

        @IntDef(OLD_BACKUP, CACHE, LAST_BACKUP, OLD_MEDIA, MEDIA)
        annotation class FileCategory
    }

    override fun compareTo(other: FileMetadata): Int {
        return importanceLevel.compareTo(other.importanceLevel)
    }
}

data class Folder(val path: String, val getFilesImportance: (files: List<File>) -> List<Pair<File, FileMetadata>>)

object Files {

    val foldersToCheck = listOf(
        Folder("/WhatsApp/Databases/") { children ->
            val sortedDb = children.sortedBy(File::lastModified)
            val dbImportance = sortedDb.mapIndexed { i: Int, db: File ->
                val metadata =
                    if(i < sortedDb.size-1)
                        FileMetadata(TRASH, OLD_BACKUP)
                    else
                        FileMetadata(NORMAL, LAST_BACKUP)
                Pair(db, metadata)
            }
            dbImportance
        },
        Folder("/WhatsApp/Media/") { children ->
            val aMonthAgoCalendar = Calendar.getInstance()
            aMonthAgoCalendar.timeInMillis -= TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS)

            children.map { file ->
                val lastModified = Calendar.getInstance()
                lastModified.timeInMillis = file.lastModified()

                val importanceLevel =
                    if(lastModified.before(aMonthAgoCalendar))
                        FileMetadata(NORMAL, OLD_MEDIA)
                    else
                        FileMetadata(IMPORTANT, MEDIA)
                Pair(file, importanceLevel)
            }
        },
        Folder("/tencent/MicroMsg/") { children ->
            children.map { child ->
                Pair(child, FileMetadata(LOW, CACHE))
            }
        }
    )

    fun getFilesToCheck(internalStorage: File): List<FileCached> {
        val files = ArrayList<Pair<File, FileMetadata>>()

        for((path, fileImportance) in foldersToCheck) {
            val fileList = File(internalStorage, path).walk().filter { it.isFile }.toList() //TODO fix walker
            files.addAll(fileImportance(fileList))
        }

        return files.map { (file, importance) ->
            FileCached(file, file.absolutePath, file.length(), importance)
        }.sortedWith(compareBy(FileCached::metadata, FileCached::size))
    }

    fun getFilesUntilSize(internalStorage: File, spaceToFreeUrgently: Long, spaceToFreePreferably: Long): Array<List<File>> {
        var counting = 0L
        val fileListByCategory = Array<MutableList<File>>(AMOUNT_OF_FILE_CATEGORIES) { mutableListOf() }


        for (file in getFilesToCheck(internalStorage)) {
            if (counting >= spaceToFreeUrgently
                && (counting >= spaceToFreePreferably || file.metadata.importanceLevel >= NORMAL))
            {
                break
            }

            fileListByCategory[file.metadata.fileCategory].add(file.file)
            counting += file.size
        }

        return fileListByCategory as Array<List<File>>
    }

    fun processInternal(): Array<List<File>> {
        val spaceAvailable = getAvailableInternalMemorySize()
        val spaceToFreeUrgently = spaceNeededAlways - spaceAvailable
        val spaceToFreePreferably = spaceNeededPreferably - (spaceAvailable - spaceToFreeUrgently)

        val internalStorageRootFile = getInternalDirectory()
        return getFilesUntilSize(internalStorageRootFile, spaceToFreeUrgently, spaceToFreePreferably)
    }

    fun getInternalDirectory(): File {
        return Environment.getExternalStorageDirectory().absolutePath.trimEnd('/').toFile()
    }

    val availableSpaceInternalPercentage: Float get() {
        return getAvailableInternalMemorySize().toFloat() / getTotalInternalMemorySize().toFloat()
    }

    val spaceNeededAlways: Long get() {
        return (getTotalInternalMemorySize() * 0.10).toLong()
    }

    val spaceNeededPreferably: Long get() {
        return (getTotalInternalMemorySize() * 0.20).toLong()
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


