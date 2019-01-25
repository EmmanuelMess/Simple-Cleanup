/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.io

import android.os.Build
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files

/**
 * Deletes a directory recursively.
 *
 * @param directory directory to delete
 * @throws IOException              in case deletion is unsuccessful
 * @throws IllegalArgumentException if `directory` does not exist or is not a directory
 */
fun deleteDirectory(directory: File): Boolean {
    try {
        if (!directory.exists()) {
            return true
        }

        if (!isSymlink(directory)) {
            cleanDirectory(directory)
        }

        if (!directory.delete()) {
            val message = "Unable to delete directory $directory."
            throw IOException(message)
        }
    } catch (e: IOException) {
        return false
    }

    return true
}

/**
 * Cleans a directory without deleting it.
 *
 * @param directory directory to clean
 * @throws IOException              in case cleaning is unsuccessful
 * @throws IllegalArgumentException if `directory` does not exist or is not a directory
 */
@Throws(IOException::class)
fun cleanDirectory(directory: File) {
    val files = verifiedListFiles(directory)

    var exception: IOException? = null
    for (file in files) {
        try {
            forceDelete(file)
        } catch (ioe: IOException) {
            exception = ioe
        }

    }

    if (null != exception) {
        throw exception
    }
}


/**
 * Lists files in a directory, asserting that the supplied directory satisfies exists and is a directory
 * @param directory The directory to list
 * @return The files in the directory, never null.
 * @throws IOException if an I/O error occurs
 */
@Throws(IOException::class)
private fun verifiedListFiles(directory: File): Array<File> {
    if (!directory.exists()) {
        val message = directory.toString() + " does not exist"
        throw IllegalArgumentException(message)
    }

    if (!directory.isDirectory) {
        val message = directory.toString() + " is not a directory"
        throw IllegalArgumentException(message)
    }

    return directory.listFiles()
        ?: // null if security restricted
        throw IOException("Failed to list contents of $directory")
}

/**
 * Deletes a file. If file is a directory, delete it and all sub-directories.
 *
 *
 * The difference between File.delete() and this method are:
 *
 *  * A directory to be deleted does not have to be empty.
 *  * You get exceptions when a file or directory cannot be deleted.
 * (java.io.File methods returns a boolean)
 *
 *
 * @param file file or directory to delete, must not be `null`
 * @throws NullPointerException  if the directory is `null`
 * @throws FileNotFoundException if the file was not found
 * @throws IOException           in case deletion is unsuccessful
 */
@Throws(IOException::class)
fun forceDelete(file: File) {
    if (file.isDirectory) {
        deleteDirectory(file)
    } else {
        val filePresent = file.exists()
        if (!file.delete()) {
            if (!filePresent) {
                throw FileNotFoundException("File does not exist: $file")
            }
            val message = "Unable to delete file: $file"
            throw IOException(message)
        }
    }
}

/**
 * Determines whether the specified file is a Symbolic Link rather than an actual file.
 *
 *
 * Will not return true if there is a Symbolic Link anywhere in the path,
 * only if the specific file is.
 *
 *
 * When using jdk1.7, this method delegates to `boolean java.nio.file.Files.isSymbolicLink(Path path)`
 *
 * **Note:** the current implementation always returns `false` if running on
 * jkd1.6 and the system is detected as Windows using [FilenameUtils.isSystemWindows]
 *
 *
 * For code that runs on Java 1.7 or later, use the following method instead:
 * <br></br>
 * `boolean java.nio.file.Files.isSymbolicLink(Path path)`
 * @param file the file to check
 * @return true if the file is a Symbolic Link
 * @throws IOException if an IO error occurs while checking the file
 * @since 2.0
 */
@Throws(IOException::class)
fun isSymlink(file: File): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        return Files.isSymbolicLink(file.toPath())
    }

    var fileInCanonicalDir: File? = null
    if (file.parent == null) {
        fileInCanonicalDir = file
    } else {
        val canonicalDir = file.parentFile.canonicalFile
        fileInCanonicalDir = File(canonicalDir, file.name)
    }

    return if (fileInCanonicalDir.canonicalFile == fileInCanonicalDir.absoluteFile) {
        isBrokenSymlink(file)
    } else {
        true
    }
}

/**
 * Determines if the specified file is possibly a broken symbolic link.
 *
 * @param file the file to check
 * @return true if the file is a Symbolic Link
 * @throws IOException if an IO error occurs while checking the file
 */
@Throws(IOException::class)
private fun isBrokenSymlink(file: File): Boolean {
    // if file exists then if it is a symlink it's not broken
    if (file.exists()) {
        return false
    }
    // a broken symlink will show up in the list of files of its parent directory
    val canon = file.canonicalFile
    val parentDir = canon.parentFile
    if (parentDir == null || !parentDir.exists()) {
        return false
    }

    // is it worthwhile to create a FileFilterUtil method for this?
    // is it worthwhile to create an "identity"  IOFileFilter for this?
    val fileInDir = parentDir.listFiles { aFile -> aFile == canon }
    return fileInDir != null && fileInDir.isNotEmpty()
}