package com.applidium.demorxsearch

import android.arch.lifecycle.ViewModel
import android.util.Log

class SearchViewModel: ViewModel() {
    fun onSearchInput(searchQuery: String) {
        // todo search
        Log.d("search", "search query = $searchQuery")
    }
}
