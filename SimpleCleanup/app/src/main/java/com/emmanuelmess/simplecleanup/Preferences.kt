package com.emmanuelmess.simplecleanup

import android.app.Activity
import android.content.Context

object Preferences {
    const val SHARED_PREFERENCES_FILE_NAME = "SimpleCleanupPreferences"
    const val SPACE_LEFT_PREFERENCE = "spaceLeftPref"

    /**
     * This sets the current [SpaceState] as limit of how much storge can be cleaned
     */
    fun setCurrentSpaceStateAsUpperBound(context: Context) {
        val available = Files.availableSpaceInternalPercentage

        @SpaceState val spaceState: Int =
            if(available < CONSERVATIVE_FRAGMENTATION_THRESHOLD) FRAGMENTING
            else if(available < MIN_NORMAL_FREE_SPACE) LOW
            else NORMAL

        getPrefs(context)
            .edit()
            .putInt(SPACE_LEFT_PREFERENCE, spaceState)
            .apply()
    }

    @SpaceState
    fun getCurrentSpaceStateUpperBound(context: Context)
        = getPrefs(context)
            .getInt(SPACE_LEFT_PREFERENCE, NORMAL)

    private fun getPrefs(context: Context)
            = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
}