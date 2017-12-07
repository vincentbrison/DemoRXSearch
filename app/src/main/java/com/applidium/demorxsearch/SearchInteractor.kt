package com.applidium.demorxsearch

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver

class SearchInteractor {

    private val memberRepository = MemberRepository()
    private val disposables = CompositeDisposable()

    fun execute(querySource: Observable<String>, listener: DisposableObserver<Set<String>>) {
        querySource
            .map { query -> query.normalize() }
            .flatMapSingle { searchQuery -> memberRepository.search(searchQuery) }
            .subscribe(listener)
        disposables.add(listener)
    }

    fun clear() {
        disposables.dispose()
    }
}
