package com.applidium.demorxsearch

import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class SearchInteractor {

    private val memberRepository = MemberRepository()
    private val disposables = CompositeDisposable()

    fun execute(
        querySource: Observable<String>,
        listener: DisposableObserver<Either<Throwable, Set<String>>>
    ) {
        querySource
            .map { query -> query.normalize() }
            .doOnEach { searchQuery ->
                Log.d("search", "search query = ${searchQuery.value}")
            }
            .debounce(1, TimeUnit.SECONDS)
            .doOnEach { searchQuery ->
                Log.d(
                    "search", "Debounce search query = ${searchQuery.value}"
                )
            }
            .switchMapSingle { searchQuery ->
                memberRepository
                    .search(searchQuery)
                    .subscribeOn(Schedulers.io())
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(listener)
        disposables.add(listener)
    }

    fun clear() {
        disposables.dispose()
    }
}
