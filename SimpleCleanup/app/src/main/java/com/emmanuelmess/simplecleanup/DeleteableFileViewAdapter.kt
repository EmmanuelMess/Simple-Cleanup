package com.emmanuelmess.simplecleanup

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.emmanuelmess.simplecleanup.FileMetadata.Companion.LAST_BACKUP
import com.emmanuelmess.simplecleanup.FileMetadata.Companion.MEDIA
import com.emmanuelmess.simplecleanup.FileMetadata.Companion.OLD_MEDIA
import com.emmanuelmess.simplecleanup.extensions.let
import kotlinx.android.synthetic.main.item_filelist.view.*

class DeleteableFileViewAdapter(val context: Context): RecyclerView.Adapter<FileHolder>() {
    private var itemsOfList: List<Model> = listOf()

    init {
        setHasStableIds(false)
    }

    fun setCategories(
        oldBackups: Boolean,
        cache: Boolean,
        lastBackup: Boolean,
        oldMedia: Boolean,
        media: Boolean
    ) {
        val items = mutableListOf<Model>()

        if(oldBackups) items.add(Model(R.string.old_backup))
        if(cache) items.add(Model(R.string.cache))
        if(lastBackup) items.add(Model(R.string.last_backup))
        if(oldMedia) items.add(Model(R.string.old_media))
        if(media) items.add(Model(R.string.media))

        this.itemsOfList = items

        notifyDataSetChanged()
    }

    fun setSuccessOrFaliure(success: Boolean, index: Int) {
        itemsOfList[index].finished = true
        itemsOfList[index].success = success
        notifyItemChanged(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_filelist, parent, false)
        return FileHolder(view as ConstraintLayout)
    }

    override fun getItemCount() = itemsOfList.size

    override fun onBindViewHolder(holder: FileHolder, position: Int) = itemsOfList[position].let { model ->
        holder.setFile(model.categoryText)
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

    fun setFile(@StringRes categoryText: Int) = with(layout) {
        nameTextView.setText(categoryText)
    }

    fun setNotFinished() = with(layout) {
        nameTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
    }

    fun setFinished(success: Boolean) = with(layout) {
        val icon = if(success) R.drawable.ic_delete_forever_green_24dp else R.drawable.ic_error_red_24dp
        nameTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, icon, 0)
    }
}

data class Model(@StringRes val categoryText: Int, var finished: Boolean = false, var success: Boolean? = null) {
    override fun hashCode(): Int {
        var hash = 37
        hash = 37 * hash + categoryText.hashCode()
        hash = 37 * hash + (if(finished) 0 else 1)
        let(success) { success ->
            hash = 37 * hash + (if(success == null || success) 0 else 1)
        }
        return hash
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Model

        if (categoryText != other.categoryText) return false
        if (finished != other.finished) return false
        if (success != other.success) return false

        return true
    }
}