package com.emmanuelmess.simplecleanup

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.emmanuelmess.simplecleanup.extensions.let
import kotlinx.android.synthetic.main.item_filelist.view.*
import java.io.File

class DeleteableFileViewAdapter(val context: Context): RecyclerView.Adapter<FileHolder>() {
    private var files: List<Model> = listOf()

    init {
        setHasStableIds(false)
    }

    fun setFiles(files: List<File>) {
        this.files = files.map { Model(it, false, null) }
        notifyDataSetChanged()
    }

    fun setSuccessOrFaliure(success: Boolean, index: Int) {
        files[index].finished = true
        files[index].success = success
        notifyItemChanged(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_filelist, parent, false)
        return FileHolder(view as ConstraintLayout)
    }

    override fun getItemCount() = files.size

    override fun onBindViewHolder(holder: FileHolder, position: Int) = files[position].let { model ->
        holder.setFile(model.file)
        if(!model.finished) {
            holder.setNotFinished()
        } else {
            holder.setFinished(model.success!!)
        }
    }

}

class FileHolder(
    private val layout: ConstraintLayout
) : RecyclerView.ViewHolder(layout) {

    fun setFile(file: File) = with(layout) {
        nameTextView.text = file.absolutePath
    }

    fun setNotFinished() = with(layout) {
        nameTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
    }

    fun setFinished(success: Boolean) = with(layout) {
        val icon = if(success) R.drawable.ic_delete_forever_green_24dp else R.drawable.ic_error_red_24dp
        nameTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, icon, 0)
    }
}

data class Model(val file: File, var finished: Boolean, var success: Boolean?) {
    override fun hashCode(): Int {
        var hash = 37
        hash = 37 * hash + (if(finished) 0 else 1)
        let(success) { success ->
            hash = 37 * hash + (if(success == null || success) 0 else 1)
        }
        return file.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Model

        if (file != other.file) return false
        if (finished != other.finished) return false
        if (success != other.success) return false

        return true
    }
}