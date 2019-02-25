package com.emmanuelmess.simplecleanup.extensions

import java.io.File

fun List<File>.deleteAll(): Boolean {
    forEach { file ->
        if(!file.delete()) return false
    }

    return true
}