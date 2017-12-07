package com.applidium.demorxsearch

import android.arch.lifecycle.ViewModel
import android.util.Log
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

class SearchViewModel : ViewModel() {

    private val publisher = PublishSubject.create<String>()
    private val searchInteractor = SearchInteractor()

    init {
        searchInteractor.execute(publisher, makeSearchResultListener())
    }

    private fun makeSearchResultListener(): DisposableObserver<Either<Throwable, Set<String>>> {
        return object : DisposableObserver<Either<Throwable, Set<String>>>() {
            override fun onNext(t: Either<Throwable, Set<String>>) {
                t.fold({ onError(it) }, { onResult(it) })
            }

            override fun onError(e: Throwable) {
                Log.e("search", "Something went wrong on search", e)
            }

            override fun onComplete() {
                // no-op
            }

        }
    }

    private fun onResult(searchResult: Set<String>) {
        Log.d("search", "search result = $searchResult")
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
