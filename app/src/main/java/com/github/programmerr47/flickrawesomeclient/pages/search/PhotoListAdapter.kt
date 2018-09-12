package com.github.programmerr47.flickrawesomeclient.pages.search

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.github.programmerr47.flickrawesomeclient.widgets.lists.DiffCallbackFactory
import com.github.programmerr47.flickrawesomeclient.models.Photo
import com.github.programmerr47.flickrawesomeclient.R
import com.github.programmerr47.flickrawesomeclient.widgets.lists.SimpleDiffCallback
import com.github.programmerr47.flickrawesomeclient.widgets.lists.BindRecyclerHolder
import com.github.programmerr47.flickrawesomeclient.util.calculateDiff
import com.github.programmerr47.flickrawesomeclient.util.dispatchUpdatesFrom
import com.github.programmerr47.flickrawesomeclient.util.inflater
import com.squareup.picasso.Picasso

typealias OnPhotoItemClickListener = (Context, Int) -> Unit

class PhotoListAdapter(
        private val onPhotoItemClickListener: OnPhotoItemClickListener,
        private val diffFactory: DiffCallbackFactory<Photo> = { o, n -> SimpleDiffCallback(o, n) }
) : RecyclerView.Adapter<PhotoListAdapter.ViewHolder>() {
    private var photoList: List<Photo> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(parent.inflater().inflate(R.layout.item_photo, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photo = photoList[position]
        holder.run {
            titleView.text = photo.title
            photoView.setOnClickListener { onPhotoItemClickListener(it.context, position) }

            Log.v("FUCK", "holder.photo.width: ${holder.photoView.width}")
            Log.v("FUCK", "holder.photo.height: ${holder.photoView.height}")
            Log.v("FUCK", "holder.photo.mWidth: ${holder.photoView.measuredWidth}")
            Log.v("FUCK", "holder.photo.mHeight: ${holder.photoView.measuredHeight}")
            Picasso.get().load(photo.generateUrl())
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.photo_placeholder)
                    .into(photoView)
        }
    }

    override fun getItemCount() = photoList.size

    fun update(newList: List<Photo>) {
        val diffResult = diffFactory(photoList, newList).calculateDiff()
        photoList = newList
        dispatchUpdatesFrom(diffResult)
    }

    class ViewHolder(itemView: View) : BindRecyclerHolder(itemView) {
        val titleView = bind<TextView>(R.id.tv_title)
        val photoView = bind<ImageView>(R.id.riv_photo)
    }
}
