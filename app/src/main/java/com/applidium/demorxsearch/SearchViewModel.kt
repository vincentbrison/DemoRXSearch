package com.applidium.demorxsearch

import android.arch.lifecycle.ViewModel
import android.util.Log
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

class SearchViewModel: ViewModel() {

    private val publisher = PublishSubject.create<String>()
    private val searchInteractor = SearchInteractor()

    init {
        searchInteractor.execute(publisher, makeSearchResultListener())
    }

    private fun makeSearchResultListener(): DisposableObserver<Set<String>> {
        return object : DisposableObserver<Set<String>>() {
            override fun onNext(result: Set<String>) {
                Log.d("search", "search result = $result")
            }

            override fun onError(e: Throwable) {
                Log.e("search", "Something went wrong on search", e)
            }

            override fun onComplete() {
                // no-op
            }

        }
    }

    fun onSearchInput(searchQuery: String) {
        Log.d("search", "search query = $searchQuery")
        publisher.onNext(searchQuery)
    }

    override fun onCleared() {
        searchInteractor.clear()
        super.onCleared()
    }
}
