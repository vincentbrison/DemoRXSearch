package com.applidium.demorxsearch

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import android.util.Log
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import io.reactivex.Observable
import io.reactivex.processors.PublishProcessor
import io.reactivex.rxkotlin.ofType

class SearchViewModel : ViewModel() {

    private val searchInteractor = SearchInteractor()
    private val viewStateLiveData: LiveData<ViewState>
    private val viewEventsStream = PublishProcessor.create<ViewEvent>()

    fun viewState(): LiveData<ViewState> = viewStateLiveData

    init {
        val viewStateStream = viewEventsStream
            .ofType<ViewEvent.StringQuery>()
            .doOnNext { Log.d("rxdebug", "value : $it") }
            .map { it.value }
            .replay(
                { shared ->
                    Flowable.merge(
                        viewEventsStream
                            .ofType<ViewEvent.Retry>()
                            .flatMapMaybe { shared.firstElement() }
                        ,
                        shared
                    )
                },
                1)
            .doOnNext {
                Log.d("rxdebug", "upstream : $it")
            }
            .compose(getDataTransformer())
            .doOnNext {
                Log.d("rxdebug", "downstream : ${it.javaClass.simpleName}")
            }
        viewStateLiveData = LiveDataReactiveStreams.fromPublisher(viewStateStream.cache())
    }

    private fun getDataTransformer(): FlowableTransformer<String, ViewState> {
        return FlowableTransformer {
            it.compose(searchInteractor).map {
                when (it) {
                    is Output.Result -> ViewState.Content(it.value.toString())
                    is Output.InFlight -> ViewState.Loading
                    is Output.Error -> ViewState.Error(it.throwable.localizedMessage)
                }
            }
        }
    }

    fun setUIEventStream(upstream: Observable<ViewEvent>) {
        upstream.toFlowable(BackpressureStrategy.LATEST).subscribe(viewEventsStream)
    }
}

sealed class ViewState {
    object Loading : ViewState()
    data class Error(val message: String) : ViewState()
    data class Content(val result: String) : ViewState()
}

sealed class ViewEvent {
    data class StringQuery(val value: String) : ViewEvent()
    object Retry : ViewEvent()
}
