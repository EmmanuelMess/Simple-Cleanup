package com.emmanuelmess.simplecleanup

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.recyclerview.widget.LinearLayoutManager
import com.emmanuelmess.simplecleanup.extensions.deleteAll
import com.emmanuelmess.simplecleanup.helpers.PermissionsActivity
import com.emmanuelmess.simplecleanup.helpers.isStorageFragmenting
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.apache.commons.io.deleteDirectory
import java.io.File
import kotlin.math.roundToInt

class MainActivity : PermissionsActivity() {

    var filesByCategory: List<List<File>>? = null
    lateinit var adapter: DeleteableFileViewAdapter
    var spaceLeftSnackbar: ResetableSnackbar? = null

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
            GetDeletableFiles(this).onPostExecute { files: Array<List<File>> ->
                pullToRefresh.isRefreshing = false
                loadFilesList(files)
            }.execute()
        }
    }

    private fun loadFilesList(files: Array<List<File>>) {
        val nonEmptyCategories = files.map { it.isNotEmpty() }
        (filesRecyclerView.adapter as DeleteableFileViewAdapter)
            .setCategories(
                nonEmptyCategories[0],
                nonEmptyCategories[1],
                nonEmptyCategories[2],
                nonEmptyCategories[3],
                nonEmptyCategories[4]
            )

        filesByCategory = files.filter { it.isNotEmpty() }

        filesByCategory!!.let { filesByCategory ->
            if (filesByCategory.isNotEmpty()) {
                fab.show()
                emptyState.visibility = GONE
            } else {
                fab.hide()
                emptyState.visibility = VISIBLE
            }

            if (filesByCategory.isNotEmpty()) {
                refreshInternalStorageLeftSnackbar()
            } else {
                spaceLeftSnackbar?.snackbar?.dismiss()
                spaceLeftSnackbar = null
            }
        }
    }

    private fun delete() {
        fab.hide()

        filesByCategory?.let { files ->
            for (i in 0 until files.size) {
                val success = files[i].deleteAll()
                adapter.setSuccessOrFaliure(success, i)
            }
        }

        refreshInternalStorageLeftSnackbar()
    }

    private fun refreshInternalStorageLeftSnackbar() {
        val text = getString(R.string.space_left_percentage, Files.availableSpaceInternalPercentage.roundToInt())

        if(spaceLeftSnackbar == null) {
            spaceLeftSnackbar = ResetableSnackbar(Snackbar.make(filesRecyclerView, text, Snackbar.LENGTH_INDEFINITE))
        } else {
            spaceLeftSnackbar!!.snackbar.setText(text)
            if(isStorageFragmenting(this, Files.getInternalDirectory())) {
                spaceLeftSnackbar!!.setColor(R.color.colorError)
            } else {
                spaceLeftSnackbar!!.resetColor()
            }
        }

        spaceLeftSnackbar!!.snackbar.show()
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