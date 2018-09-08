package com.github.programmerr47.flickrawesomeclient

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.github.programmerr47.flickrawesomeclient.util.calculateDiff
import com.github.programmerr47.flickrawesomeclient.util.dispatchUpdatesFrom
import com.github.programmerr47.flickrawesomeclient.util.inflater
import com.squareup.picasso.Picasso

class PhotoListAdapter(
        private val diffFactory: DiffCallbackFactory<Photo> = { o, n -> SimpleDiffCallback(o, n) }
) : RecyclerView.Adapter<PhotoListAdapter.ViewHolder>() {
    private var photoList: List<Photo> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(parent.inflater().inflate(R.layout.item_photo, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(photoList[position])
    }

    override fun getItemCount() = photoList.size

    fun update(newList: List<Photo>) {
        val diffResult = diffFactory(photoList, newList).calculateDiff()
        photoList = newList
        dispatchUpdatesFrom(diffResult)
    }

    class ViewHolder(itemView: View) : BindRecyclerHolder(itemView) {
        private val titleView = bind<TextView>(R.id.tv_title)
        private val photoView = bind<ImageView>(R.id.riv_photo)

        fun bind(photo: Photo) {
            titleView.text = photo.title
            Picasso.get().load(photo.generateUrl())
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.photo_placeholder)
                    .into(photoView)
        }
    }
}
