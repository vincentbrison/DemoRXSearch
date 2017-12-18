package com.applidium.demorxsearch

import android.os.Looper
import android.util.Log
import io.reactivex.Single

class MemberRepository {

    private val data = setOf("EllaKash", "MarlaLebo", "KenethRey", "ElsaPeter", "EarlineRolph",
        "MackenzieAzcona", "EldenSpino", "AugustineCrecelius", "LinneaAbeyta", "AnglaBlough",
        "DrewMccaskill", "SophiaStolz", "DelorasDear", "AlesiaSchack", "VedaBonaparte",
        "Inger Maynes")

    fun searchAsSingle(searchQuery: String): Single<Either<Throwable, Set<String>>> {
        return Single.fromCallable {
            return@fromCallable search(searchQuery)
        }.either()
    }

    fun search(searchQuery: String): Set<String> {
        checkNotMainThread()
        simulateSearchDuration(searchQuery)
        Log.d("threading", "Finishing blocking operation on thread ${Thread.currentThread().id}")
        return data.filter { name ->
            name.contains(searchQuery, ignoreCase = true)
        }.toSet()
    }

    private fun checkNotMainThread() {
        Log.d("threading", "Doing blocking operation on thread ${Thread.currentThread().id}")
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw IllegalThreadStateException("Blocking operation on main thread")
        }
    }

    private fun simulateSearchDuration(searchQuery: String) {
        val searchComplexity = Math.max(5 - searchQuery.length, 0)
        val searchDurationMs = searchComplexity * 1000L
        try {
            Thread.sleep(searchDurationMs)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}
