package com.emmanuelmess.simplecleanup.helpers

import android.app.Activity
import android.os.AsyncTask
import java.lang.ref.WeakReference

abstract class AsyncTaskWithCallback<Params, Progress, Result>(
    fatherActivity: Activity
): AsyncTask<Params, Progress, Result>() {

    val fatherActivity = WeakReference(fatherActivity)

    private var callback: (Activity.(Result) -> Unit)? = null

    fun onPostExecute(callback: Activity.(Result) -> Unit): AsyncTaskWithCallback<Params, Progress, Result> {
        this.callback = callback
        return this
    }

    override fun onPostExecute(result: Result) {
        val fatherActivity = this.fatherActivity.get()
        if(fatherActivity != null) {
            callback?.invoke(fatherActivity, result)
        }
    }
}