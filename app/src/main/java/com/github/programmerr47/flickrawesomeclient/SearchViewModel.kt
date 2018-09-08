package com.github.programmerr47.flickrawesomeclient

import android.arch.lifecycle.ViewModel

class SearchViewModel : ViewModel() {
    var searchText: String = ""
    var searchResult: PhotoList? = null
}