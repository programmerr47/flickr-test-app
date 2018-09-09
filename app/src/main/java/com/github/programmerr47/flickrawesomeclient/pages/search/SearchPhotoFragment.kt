package com.github.programmerr47.flickrawesomeclient.pages.search

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import com.github.programmerr47.flickrawesomeclient.*
import com.github.programmerr47.flickrawesomeclient.models.PhotoList
import com.github.programmerr47.flickrawesomeclient.services.FlickrSearcher
import com.github.programmerr47.flickrawesomeclient.util.*
import com.github.programmerr47.flickrawesomeclient.util.sugar.textWatcher
import com.github.programmerr47.flickrawesomeclient.widgets.lists.GridSpacingItemDecoration
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.SupportFragmentInjector
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.Disposable

class SearchPhotoFragment : Fragment(), SupportFragmentInjector {
    override val injector: KodeinInjector = KodeinInjector()
    override fun provideOverridingModule() = Kodein.Module {
        bind<SearchViewModel>() with provider {
            ViewModelProviders.of(instance<AppCompatActivity>("activity"))[SearchViewModel::class.java]
        }
    }

    private val flickrSearcher: FlickrSearcher by injector.instance()
    private val searchViewModel: SearchViewModel by injector.instance()

    private var searchView: EditText? = null
    private var listAdapter: PhotoListAdapter? = null
    private var swipeProgressView: SwipeRefreshLayout? = null

    private var searchDisposable: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initializeInjector()
        return inflater.inflate(R.layout.fragment_search_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.run {
            val toolbarShadowHeight = context.resources.getDimension(R.dimen.toolbar_shadow_height)

            findViewById<Toolbar>(R.id.toolbar).title = getString(R.string.page_search_title)
            findViewById<AppBarLayout>(R.id.appBarLayout)
                    .setStateListElevationAnimator(toolbarShadowHeight)

            findViewById<ImageView>(R.id.iv_search).setOnClickListener { applySearch() }
            findViewById<RecyclerView>(R.id.rv_list).init()

            searchView = initSearchView(findViewById(R.id.et_search))
            swipeProgressView = findViewById<SwipeRefreshLayout>(R.id.srl_refresh).apply {
                setOnRefreshListener { refreshSearch() }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        searchView?.setText(searchViewModel.searchText)
        searchViewModel.searchResult?.let { listAdapter?.update(it.list) }
    }

    override fun onDestroyView() {
        swipeProgressView = null
        listAdapter = null
        searchView = null
        destroyInjector()
        super.onDestroyView()
    }

    private fun initSearchView(editText: EditText) = editText.apply {
        addTextChangedListener(textWatcher().after {
            searchViewModel.searchText = it.toString()
        })
        setOnImeOptionsClickListener { applySearch() }

        if (searchViewModel.searchText.isEmpty()) showKeyboard()
    }

    private fun RecyclerView.init() {
        val spanCount = context.resources.getInteger(R.integer.page_search_column_count)
        val gridPadding = context.resources.getDimensionPixelSize(R.dimen.padding_small)

        layoutManager = GridLayoutManager(context, spanCount)
        addItemDecoration(GridSpacingItemDecoration(spanCount, gridPadding))
        adapter = PhotoListAdapter().also { listAdapter = it }

        addOnScrollListener(createLoadMoreDetector())
    }

    private fun createLoadMoreDetector() = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val visibleCount = recyclerView.layoutManager.childCount
            val totalCount = recyclerView.layoutManager.itemCount
            val firstVisiblePos = (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition() ?: 0

            if (visibleCount + firstVisiblePos >= totalCount && searchDisposable?.isDisposed != false) {
                searchDisposable = applySearch(FlickrSearcher::searchMorePhotos)
            }
        }
    }

    private fun applySearch() = applySearch(FlickrSearcher::searchPhotos)
    private fun refreshSearch() = applySearch(FlickrSearcher::searchForce)

    private inline fun applySearch(searchFun: FlickrSearcher.(String) -> Single<PhotoList>) =
            applySearch(searchViewModel.searchText, searchFun)

    private inline fun applySearch(text: String, searchFun: FlickrSearcher.(String) -> Single<PhotoList>): Disposable {
        hideKeyboard()
        swipeProgressView?.isRefreshing = true
        return flickrSearcher.searchFun(text)
                .observeOn(mainThread())
                .doFinally { swipeProgressView?.isRefreshing = false }
                .subscribe(
                        {
                            searchViewModel.searchResult = it
                            listAdapter?.update(it.list)
                        },
                        { showToast(it.localizedMessage) }
                )
    }
}