package com.github.programmerr47.flickrawesomeclient.pages.gallery

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import com.github.chrisbanes.photoview.PhotoView
import com.github.programmerr47.flickrawesomeclient.models.Photo
import com.github.programmerr47.flickrawesomeclient.R
import com.github.programmerr47.flickrawesomeclient.widgets.FlingLayout
import com.github.programmerr47.flickrawesomeclient.util.*
import com.squareup.picasso.Picasso
import io.reactivex.disposables.Disposable

class GalleryActivity : AppCompatActivity() {
    private lateinit var systemUiSwitch: SystemUiVisibilitySwitch
    private lateinit var photo: Photo

    private val toolbar: Toolbar by bindable(R.id.toolbar)
    private val rootView: View by bindable(R.id.fl_root)

    private var systemUiSwitchDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        photo = (savedInstanceState ?: intent.extras).getParcelable(EXTRA_PHOTO)
        systemUiSwitch = DefaultSystemUiSwitch(window.decorView)

        initToolbar(toolbar)
        initPhotoView(findViewById(R.id.fl_photo_container), findViewById(R.id.pv_photo), photo)
    }

    override fun onStart() {
        super.onStart()
        systemUiSwitchDisposable = systemUiSwitch.observe().subscribe { visible ->
            toolbar.let {
                if (visible) it.revealSlide() else it.fadeSlideUp()
            }
        }
        if (!systemUiSwitch.isSystemUiVisible()) {
            systemUiSwitch.hideSystemUi()
        }
    }

    override fun onStop() {
        if (systemUiSwitchDisposable?.isDisposed == false) {
            systemUiSwitchDisposable?.dispose()
            systemUiSwitchDisposable = null
        }
        super.onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun initToolbar(toolbar: Toolbar) {
        toolbar.title = photo.title

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(getDrawableCompat(R.drawable.ic_toolbar_back))
    }

    private fun initPhotoView(photoContainer: FlingLayout, photoView: PhotoView, photo: Photo) {
        photoContainer.run {
            dismissListener = { finishNoAnim() }
            positionChangeListener = { _, _, factor -> changeContentTransparency(factor) }
        }

        photoView.run {
            setOnScaleChangeListener { factor, _, _ -> photoContainer.isDragEnabled = factor <= 1.5f }
            setOnViewTapListener { _, _, _ ->
                systemUiSwitch.run {
                    if (isSystemUiVisible()) hideSystemUi() else showSystemUi()
                }
            }
            Picasso.get().load(photo.generateUrl())
                    .fit()
                    .centerInside()
                    .into(this)
        }
    }

    private fun changeContentTransparency(factor: Float) {
        rootView.setBackgroundColor(Color.argb(Math.round(255 * (1f - factor)), 0, 0, 0))
        toolbar.alpha = 1f - factor * 3
    }

    companion object {
        private const val EXTRA_PHOTO = "EXTRA_PHOTOS"

        fun open(context: Context, photo: Photo) = context.startActivity(GalleryActivity::class) {
            putExtra(EXTRA_PHOTO, photo)
        }
    }
}

