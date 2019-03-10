package com.emmanuelmess.simplecleanup

import android.content.Context
import androidx.annotation.IntDef
import com.emmanuelmess.simplecleanup.helpers.isStorageFragmenting

const val MIN_NORMAL_FREE_SPACE = 0.20

const val CONSERVATIVE_FRAGMENTATION_THRESHOLD = 0.10


const val FRAGMENTING = 0
const val LOW = 1
const val NORMAL = 2

@IntDef(NORMAL, LOW, FRAGMENTING)
annotation class SpaceState

@SpaceState
fun getCurrentSpaceState(context: Context): Int {
    val available = Files.availableSpaceInternalPercentage
    if(available > MIN_NORMAL_FREE_SPACE) {
        return NORMAL
    }

    val isFragmenting = isStorageFragmenting(context, Files.getInternalDirectory())
    return if(!isFragmenting) LOW
    else FRAGMENTING
}