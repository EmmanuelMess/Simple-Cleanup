package com.emmanuelmess.simplecleanup

import android.app.Activity
import com.emmanuelmess.simplecleanup.helpers.AsyncTaskWithCallback
import java.io.File

class GetDeletableFiles(
    activity: Activity
): AsyncTaskWithCallback<Unit, Unit, Array<List<File>>>(activity) {
    override fun doInBackground(vararg params: Unit?): Array<List<File>> {
        return Files.processInternal()
    }
}