package com.emmanuelmess.simplecleanup

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.recyclerview.widget.LinearLayoutManager
import com.emmanuelmess.simplecleanup.helpers.AsyncTaskWithCallback
import com.emmanuelmess.simplecleanup.helpers.PermissionsActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.apache.commons.io.deleteDirectory
import java.io.File
import kotlin.math.roundToInt

class MainActivity : PermissionsActivity() {

    var files: List<File>? = null
    lateinit var adapter: DeleteableFileViewAdapter
    var spaceLeftSnackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        pullToRefresh.setOnRefreshListener {
            refresh()
        }

        fab.setOnClickListener {
            handlePermission(WRITE_EXTERNAL_STORAGE) {
                delete()
            }
        }

        filesRecyclerView.setHasFixedSize(false)
        filesRecyclerView.layoutManager = LinearLayoutManager(this)

        adapter = DeleteableFileViewAdapter(this)
        filesRecyclerView.adapter = adapter

        refresh()
    }

    override fun onResume() {
        super.onResume()

        refreshInternalStorageLeftSnackbar()
    }

    private fun refresh() {
        emptyState.visibility = GONE
        fab.hide()
        pullToRefresh.isRefreshing = true

        handlePermission(READ_EXTERNAL_STORAGE) {
            GetDeletableFiles(this).onPostExecute { files ->
                this@MainActivity.files = files
                pullToRefresh.isRefreshing = false
                loadFilesList()
            }.execute()
        }
    }

    private fun loadFilesList() = files!!.let { files ->
        (filesRecyclerView.adapter as DeleteableFileViewAdapter).setFiles(files)

        if(files.isNotEmpty()){
            fab.show()
            emptyState.visibility = GONE
        } else {
            fab.hide()
            emptyState.visibility = VISIBLE
        }

        if(files.isNotEmpty()) {
            refreshInternalStorageLeftSnackbar()
        } else {
            spaceLeftSnackbar?.dismiss()
            spaceLeftSnackbar = null
        }
    }

    private fun delete() {
        fab.hide()

        files?.let { files ->
            for (i in 0 until files.size) {
                val success = deleteDirectory(files[i])
                adapter.setSuccessOrFaliure(success, i)
            }
        }

        refreshInternalStorageLeftSnackbar()
    }

    private fun refreshInternalStorageLeftSnackbar() {
        val text = getString(R.string.space_left_percentage, Files.availableSpaceInternalPercentage.roundToInt())

        if(spaceLeftSnackbar == null) {
            spaceLeftSnackbar = Snackbar.make(filesRecyclerView, text, Snackbar.LENGTH_INDEFINITE)
        } else {
            spaceLeftSnackbar!!.setText(text)
        }

        spaceLeftSnackbar!!.show()
    }

    val LIST_STATE_KEY = "liststate"

    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)

        // Save list state
        state.putParcelable(LIST_STATE_KEY, filesRecyclerView.layoutManager!!.onSaveInstanceState())
    }

    override fun onRestoreInstanceState(state: Bundle?) {
        super.onRestoreInstanceState(state)

        // Retrieve list state and list/item positions
        if (state != null) {
            filesRecyclerView.layoutManager!!.onRestoreInstanceState(state.getParcelable(LIST_STATE_KEY))
        }
    }

}
