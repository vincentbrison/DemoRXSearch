package com.applidium.demorxsearch

import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Publisher
import java.util.concurrent.TimeUnit

class SearchInteractor : FlowableTransformer<Input, Output> {
    private val memberRepository = MemberRepository()

    override fun apply(upstream: Flowable<Input>): Publisher<Output> {
        return upstream
            .publish { sharedUpstream ->
                Flowable.merge(
                    loadingStream(sharedUpstream),
                    getDataStream(sharedUpstream)
                )
            }
    }

    private fun loadingStream(upstream: Flowable<Input>) =
        upstream.map { Output.InFlight }

    private fun getDataStream(upstream: Flowable<Input>): Flowable<Output> {
        return upstream.map { query -> query.normalize() }
            .debounce(1, TimeUnit.SECONDS)
            .switchMap { searchQuery ->
                memberRepository
                    .search(searchQuery)
                    .map { Output.Result(it) as Output }
                    .onErrorReturn { Output.Error(it) }
                    .toFlowable()
                    .subscribeOn(Schedulers.io())
            }
    }
}

typealias Input = String
sealed class Output {
    object InFlight : Output()
    data class Result(val value: Set<String>) : Output()
    data class Error(val throwable: Throwable) : Output()
}
