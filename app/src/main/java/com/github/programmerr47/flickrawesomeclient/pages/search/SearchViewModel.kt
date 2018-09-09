package com.github.programmerr47.flickrawesomeclient.pages.search

import android.arch.lifecycle.ViewModel
import com.github.programmerr47.flickrawesomeclient.models.PhotoList

class SearchViewModel : ViewModel() {
    var searchText: String = ""
    var searchResult: PhotoList? = null
}