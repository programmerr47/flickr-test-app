package com.github.programmerr47.flickrawesomeclient.pages.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.chrisbanes.photoview.OnViewTapListener
import com.github.chrisbanes.photoview.PhotoView
import com.github.programmerr47.flickrawesomeclient.R
import com.github.programmerr47.flickrawesomeclient.models.Photo
import com.github.programmerr47.flickrawesomeclient.widgets.DismissListener
import com.github.programmerr47.flickrawesomeclient.widgets.FlingLayout
import com.github.programmerr47.flickrawesomeclient.widgets.PositionChangeListener
import com.github.programmerr47.flickrawesomeclient.widgets.paging.PagedListPagerAdapter
import com.squareup.picasso.Picasso

class FullSizePhotoAdapter(
        val onViewTapListener: OnViewTapListener,
        val dismissListener: DismissListener = {},
        val positionChangeListener: PositionChangeListener = { _, _, _ -> }
) : PagedListPagerAdapter<Photo>() {

    override fun createItem(container: ViewGroup, position: Int): Any {
        val root = LayoutInflater.from(container.context).inflate(R.layout.item_full_screen_photo, container, false)

        val flRoot = root.findViewById<FlingLayout>(R.id.fl_root).apply {
            this.dismissListener = this@FullSizePhotoAdapter.dismissListener
            this.positionChangeListener = this@FullSizePhotoAdapter.positionChangeListener
        }

        root.findViewById<PhotoView>(R.id.pv_photo).run {
            setOnScaleChangeListener { scaleFactor, _, _ -> flRoot.isDragEnabled = scaleFactor <= 1.5f }
            setOnViewTapListener(onViewTapListener)

            Picasso.get().load(pagedList?.get(position)?.generateUrl())
                    .fit()
                    .centerInside()
                    .into(this)
        }
        container.addView(root)
        return root
    }

    override fun removeItem(container: ViewGroup, position: Int, obj: Any) =
            container.removeView(obj as View)
}