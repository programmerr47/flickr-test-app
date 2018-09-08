package com.github.programmerr47.flickrawesomeclient

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import com.github.programmerr47.flickrawesomeclient.util.hideKeyboard
import com.github.programmerr47.flickrawesomeclient.util.setOnImeOptionsClickListener
import com.github.programmerr47.flickrawesomeclient.util.setStateListElevationAnimator
import com.github.programmerr47.flickrawesomeclient.util.sugar.textWatcher
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread

class SearchPhotoFragment : Fragment() {
    private val flickrSearcher: FlickrSearcher by lazy { FlickrApplication.flickrSearcher }

    private lateinit var searchViewModel: SearchViewModel

    private var searchView: EditText? = null
    private var listAdapter: PhotoListAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.run {
            findViewById<Toolbar>(R.id.toolbar).title = getString(R.string.page_search_title)
            findViewById<AppBarLayout>(R.id.appBarLayout)
                    .setStateListElevationAnimator(getToolbarShadowHeight())
            searchView = findViewById<EditText>(R.id.et_search).apply {
                addTextChangedListener(textWatcher().after {
                    searchViewModel.searchText = it.toString()
                })
                setOnImeOptionsClickListener { applySearch() }
            }
            findViewById<ImageView>(R.id.iv_search).setOnClickListener { applySearch() }
            findViewById<RecyclerView>(R.id.rv_list).run {
                layoutManager = GridLayoutManager(context, 3)
                adapter = PhotoListAdapter().also { listAdapter = it }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        searchViewModel = ViewModelProviders.of(activity!!).get(SearchViewModel::class.java)
        searchView?.setText(searchViewModel.searchText)
    }

    override fun onDestroyView() {
        listAdapter = null
        searchView = null
        super.onDestroyView()
    }

    private fun applySearch() = applySearch(searchView?.text?.toString() ?: "")

    private fun applySearch(text: String) {
        hideKeyboard()
        flickrSearcher.searchPhotos(text)
                .observeOn(mainThread())
                .subscribe(
                        {
                            Log.v("FUCK", "onSuccess $it")
                            searchViewModel.searchResult = it
                            listAdapter?.update(it.list)
                        },
                        { Log.v("FUCK", "onError $it") },
                        { Log.v("FUCK", "onFinish") }
                )
    }

    private fun getToolbarShadowHeight() =
            context?.resources?.getDimension(R.dimen.toolbar_shadow_height) ?: 0f
}