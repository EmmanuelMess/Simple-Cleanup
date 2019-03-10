package com.emmanuelmess.simplecleanup.background

import android.app.Activity
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import com.emmanuelmess.simplecleanup.Files
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit.*

class CheckSpaceLeftScheduler(val context: Context) {
    val JOB_ID = 0

    init {
        val jobScedulerService = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        val jobInfo = JobInfo.Builder(JOB_ID, ComponentName(context, CheckSpaceLeftJobService::class.java)).apply {
            setRequiresCharging(false)
            setPeriodic(getNextDelay(Files.availableSpaceInternalPercentage))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setTriggerContentMaxDelay(MILLISECONDS.convert(1, HOURS))
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setRequiresBatteryNotLow(false)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                setRequiredNetwork(null)
                setEstimatedNetworkBytes(0, 0)
            }
        }.build()

        jobScedulerService.schedule(jobInfo)
    }

    companion object {
        fun getNextDelay(percentageUsed: Float): Long {
            if(percentageUsed <= 0.2) {
                return MILLISECONDS.convert(3*7, DAYS)
            }
            if(percentageUsed <= 0.4) {
                return MILLISECONDS.convert(2*7, DAYS)
            }
            if(percentageUsed <= 0.5) {
                return MILLISECONDS.convert(7, DAYS)
            }
            if(percentageUsed <= 0.6) {
                return MILLISECONDS.convert(1, DAYS)
            }
            if(percentageUsed <= 0.7) {
                return MILLISECONDS.convert(4, HOURS)
            }
            if(percentageUsed <= 1) {
                return MILLISECONDS.convert(1, HOURS)
            }

            throw IllegalArgumentException()
        }
    }
}