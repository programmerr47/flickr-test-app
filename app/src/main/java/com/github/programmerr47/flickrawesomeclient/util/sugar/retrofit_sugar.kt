package com.github.programmerr47.flickrawesomeclient.util.sugar

import okhttp3.HttpUrl
import okhttp3.Interceptor

fun Interceptor.Chain.adjustRequestUrl(builder: HttpUrl.Builder.() -> HttpUrl.Builder) = request().let {
    it.newBuilder()
            .url(it.url().newBuilder()
                    .builder()
                    .build())
            .build()
}

fun HttpUrl.Builder.addQueryParams(vararg params: Pair<String, String?>) = apply {
    params.forEach { (key, value) ->
        addQueryParameter(key, value)
    }
}