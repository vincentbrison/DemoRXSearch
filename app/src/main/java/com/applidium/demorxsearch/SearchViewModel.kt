package com.applidium.demorxsearch

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

class SearchViewModel : ViewModel() {

    private val queryPublisher = PublishSubject.create<String>()
    private val rXsearchInteractor = RXSearchInteractor()
    private val coroutineSearchInteractor = CoroutineSearchInteractor()
    private val viewStateLiveData = MutableLiveData<ViewState>()
    private var lastQuery: String? = null
    private var currentJob: Job? = null

    init {
        rXsearchInteractor.execute(queryPublisher, makeSearchResultListener())
    }

    fun viewState(): LiveData<ViewState> = viewStateLiveData

    private fun makeSearchResultListener(): DisposableObserver<Either<Throwable, Set<String>>> {
        return object : DisposableObserver<Either<Throwable, Set<String>>>() {
            override fun onNext(t: Either<Throwable, Set<String>>) {
                t.fold({ onError(it) }, { onResult(it) })
            }

            override fun onError(e: Throwable) {
                this@SearchViewModel.onError(e)
            }

            override fun onComplete() {
                // no-op
            }

        }
    }

    fun onResult(searchResult: Set<String>) {
        Log.d("searchAsSingle", "searchAsSingle result = $searchResult")
        viewStateLiveData.value = Content(searchResult.toString())
    }

    fun onError(throwable: Throwable) {
        Log.e("searchAsSingle", "Something went wrong on searchAsSingle", throwable)
        viewStateLiveData.value = Error(throwable.localizedMessage)
    }

    fun onClickRetry() {
        val query = lastQuery
        query?.let {
            executeSearch(query)
        }
    }

    fun onSearchInput(searchQuery: String) {
        lastQuery = searchQuery
        viewStateLiveData.value = Loading()
        executeSearch(searchQuery)
    }

    private fun executeSearch(query: String) {
        executeSearchUsingRX(query)
        executeSearchUsingCoroutines(query)
    }

    private fun executeSearchUsingRX(query: String) {
        queryPublisher.onNext(query)
    }

    private fun executeSearchUsingCoroutines(query: String) {
        currentJob?.cancel()
        Log.d("threading", "trigger cancel")
        currentJob = launch(UI) {
            val result = async(CommonPool, parent = currentJob) {
                coroutineSearchInteractor.executeJob(query)
            }
            result.await().fold({ onError(it) }, { onResult(it) })
        }
    }

    override fun onCleared() {
        rXsearchInteractor.clear()
        currentJob?.cancel()
        super.onCleared()
    }
}

sealed class ViewState
class Loading : ViewState()
data class Error(val message: String?) : ViewState()
data class Content(val result: String) : ViewState()
