package com.emmanuelmess.simplecleanup.extensions

inline fun <T> let(x: T?, callback: (T?) -> Unit) = callback(x)