package com.applidium.demorxsearch

import android.util.Log
import kotlinx.coroutines.experimental.delay
import java.util.concurrent.TimeUnit


class CoroutineSearchInteractor : BaseCoroutineSearchInteractor<Set<String>, String>() {
    private val memberRepository = MemberRepository()

    override suspend fun job(params: String): Set<String> {
        delay(1, TimeUnit.SECONDS)
        Log.d("threading", "resume from delay for debouncing")
        return memberRepository.search(params)
    }
}
