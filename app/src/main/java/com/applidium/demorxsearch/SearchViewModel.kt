package com.applidium.demorxsearch

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

class SearchViewModel : ViewModel() {

    private val queryPublisher = PublishSubject.create<String>()
    private val searchInteractor = SearchInteractor()
    private val viewStateLiveData = MutableLiveData<ViewState>()
    private var lastQuery: String? = null

    init {
        searchInteractor.execute(queryPublisher, makeSearchResultListener())
    }

    fun viewState(): LiveData<ViewState> = viewStateLiveData

    private fun makeSearchResultListener(): DisposableObserver<Either<Throwable, Set<String>>> {
        return object : DisposableObserver<Either<Throwable, Set<String>>>() {
            override fun onNext(t: Either<Throwable, Set<String>>) {
                t.fold({ onError(it) }, { onResult(it) })
            }

            override fun onError(e: Throwable) {
                Log.e("search", "Something went wrong on search", e)
                viewStateLiveData.value = Error(e.localizedMessage)
            }

            override fun onComplete() {
                // no-op
            }

        }
    }

    private fun onResult(searchResult: Set<String>) {
        Log.d("search", "search result = $searchResult")
        viewStateLiveData.value = Content(searchResult.toString())
    }

    fun onClickRetry() {
        val query = lastQuery
        query?.let { queryPublisher.onNext(query) }
    }

    fun onSearchInput(searchQuery: String) {
        Log.d("search", "search query = $searchQuery")
        lastQuery = searchQuery
        viewStateLiveData.value = Loading()
        queryPublisher.onNext(searchQuery)
    }

    override fun onCleared() {
        searchInteractor.clear()
        super.onCleared()
    }
}

sealed class ViewState
class Loading: ViewState()
data class Error(val message: String): ViewState()
data class Content(val result: String): ViewState()
