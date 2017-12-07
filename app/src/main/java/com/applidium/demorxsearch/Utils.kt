package com.applidium.demorxsearch

import java.text.Normalizer

fun String.normalize(): String {
    return Normalizer.normalize(trim().toLowerCase(), Normalizer.Form.NFD)
}
