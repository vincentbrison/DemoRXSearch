package com.applidium.demorxsearch

abstract class BaseCoroutineSearchInteractor<T, R> {
    suspend fun executeJob(params: R): Either<Throwable, T> {
        return try {
            val result = job(params)
            Right(result)
        } catch (e: Throwable) {
            Left(e)
        }
    }

    protected abstract suspend fun job(params: R): T
}
