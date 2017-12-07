package com.applidium.demorxsearch

import java.text.Normalizer
import io.reactivex.Single

fun String.normalize(): String {
    return Normalizer.normalize(trim().toLowerCase(), Normalizer.Form.NFD)
}

fun <T> Single<T>.either(): Single<Either<Throwable, T>> =
    map { Right(it) as Either<Throwable, T> }.onErrorReturn { Left(it) }
