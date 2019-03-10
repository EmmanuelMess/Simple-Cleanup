package com.emmanuelmess.simplecleanup.background

import android.app.job.JobParameters
import android.app.job.JobService
import com.emmanuelmess.simplecleanup.*

class CheckSpaceLeftJobService: JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        val currentSpaceState = getCurrentSpaceState(this)
        if(currentSpaceState < NORMAL
            && Preferences.getCurrentSpaceStateUpperBound(this) > currentSpaceState) {
            LowSpaceNotification(this).show()
        }

        CheckSpaceLeftScheduler(applicationContext)

        jobFinished(params, false)

        return false
    }

    /**
     * Will not be called if [onStartJob] returns false
     */
    override fun onStopJob(params: JobParameters?) = false
}