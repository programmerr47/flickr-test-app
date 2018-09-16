package com.github.programmerr47.flickrawesomeclient.pages.search

import android.arch.paging.PagedListAdapter
import android.content.Context
import android.support.v7.util.DiffUtil
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.github.programmerr47.flickrawesomeclient.R
import com.github.programmerr47.flickrawesomeclient.models.Photo
import com.github.programmerr47.flickrawesomeclient.util.inflater
import com.github.programmerr47.flickrawesomeclient.widgets.lists.BindRecyclerHolder
import com.github.programmerr47.flickrawesomeclient.widgets.lists.SimpleItemDiffCallback
import com.squareup.picasso.Picasso

typealias OnPhotoItemClickListener = (Context, Int) -> Unit

class PhotoListAdapter(
        private val onPhotoItemClickListener: OnPhotoItemClickListener,
        itemDiffCallback: DiffUtil.ItemCallback<Photo> = SimpleItemDiffCallback()
) : PagedListAdapter<Photo, PhotoListAdapter.ViewHolder>(itemDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(parent.inflater().inflate(R.layout.item_photo, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photo = getItem(position)
        holder.run {
            titleView.text = photo?.title ?: titleView.context.getString(R.string.status_photo_loading)
            photoView.setOnClickListener { onPhotoItemClickListener(it.context, position) }

            Picasso.get().load(photo?.generateThumbUrl())
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.photo_placeholder)
                    .into(photoView)
        }
    }

    class ViewHolder(itemView: View) : BindRecyclerHolder(itemView) {
        val titleView = bind<TextView>(R.id.tv_title)
        val photoView = bind<ImageView>(R.id.riv_photo)
    }
}