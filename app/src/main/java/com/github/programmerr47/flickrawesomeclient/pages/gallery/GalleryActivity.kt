package com.github.programmerr47.flickrawesomeclient.pages.gallery

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import com.github.chrisbanes.photoview.OnViewTapListener
import com.github.programmerr47.flickrawesomeclient.models.Photo
import com.github.programmerr47.flickrawesomeclient.R
import com.github.programmerr47.flickrawesomeclient.services.FlickrSearcher
import com.github.programmerr47.flickrawesomeclient.util.*
import com.github.programmerr47.flickrawesomeclient.util.sugar.onPageChangeListener
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.AppCompatActivityInjector
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread

class GalleryActivity : AppCompatActivity(), AppCompatActivityInjector {
    override val injector: KodeinInjector = KodeinInjector()
    override fun provideOverridingModule() = Kodein.Module {
        bind<GalleryViewModel>() with provider {
            ViewModelProviders.of(this@GalleryActivity,
                    object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return GalleryViewModel().apply {
                                searchText = intent.extras.getString(EXTRA_SEARCH_QUERY)
                                currentPosition = intent.extras.getInt(EXTRA_POSITION)
                            } as T
                        }
                    })[GalleryViewModel::class.java]
        }
    }

    private val flickrSearcher: FlickrSearcher by instance()
    private val galleryViewModel: GalleryViewModel by instance()
    private val systemUiSwitch: UiSwitch = UiSwitch()

    private val toolbar: Toolbar by bindable(R.id.toolbar)
    private val rootView: View by bindable(R.id.fl_root)
    private val viewPager: ViewPager by bindable(R.id.vp_list)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        initializeInjector()

        initToolbar(toolbar)
        flickrSearcher.searchPhotos(galleryViewModel.searchText)
                .observeOn(mainThread())
                .subscribe { pagedList ->
                    initViewPager(viewPager, pagedList, galleryViewModel.currentPosition)
                }
    }

    override fun onStart() {
        super.onStart()
        systemUiSwitch.setOnUiStateChangeListener(this::updateUiVisibility)
        updateUiVisibility(systemUiSwitch.isUiVisible)
    }

    private fun updateUiVisibility(visible: Boolean) {
        toolbar.let {
            if (visible) it.revealSlide() else it.fadeSlideUp()
        }
    }

    override fun onDestroy() {
        destroyInjector()
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun initToolbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(getDrawableCompat(R.drawable.ic_toolbar_back))
    }

    private fun initViewPager(viewPager: ViewPager, photos: PagedList<Photo>, position: Int) = viewPager.run {
        adapter = FullSizePhotoAdapter(photos,
                OnViewTapListener { _, _, _ ->
                    systemUiSwitch.run { isUiVisible = !isUiVisible }
                },
                { finishNoAnim() },
                { _, _, factor -> changeContentTransparency(factor) })

        currentItem = position

        viewPager.addOnPageChangeListener(onPageChangeListener()
                .scrolled { position, _, _ ->
                    galleryViewModel.currentPosition = position
                    title = photos[position]?.title ?: viewPager.context.getString(R.string.status_photo_loading)
                })
    }

    private fun changeContentTransparency(factor: Float) {
        rootView.setBackgroundColor(Color.argb(Math.round(255 * (1f - factor)), 0, 0, 0))
        toolbar.alpha = 1f - factor * 3
    }

    companion object {
        private const val EXTRA_SEARCH_QUERY = "EXTRA_SEARCH_QUERY"
        private const val EXTRA_POSITION = "EXTRA_POSITION"

        fun open(context: Context, searchQuery: String, position: Int) = context.startActivity(GalleryActivity::class) {
            putExtra(EXTRA_SEARCH_QUERY, searchQuery)
            putExtra(EXTRA_POSITION, position)
        }
    }
}

