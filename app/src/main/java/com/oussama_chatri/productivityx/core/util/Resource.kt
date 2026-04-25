package com.oussama_chatri.productivityx.core.util

sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val code: String? = null) : Resource<Nothing>()
}

fun <T, R> Resource<T>.map(transform: (T) -> R): Resource<R> = when (this) {
    is Resource.Success -> Resource.Success(transform(data))
    is Resource.Error -> Resource.Error(message, code)
    Resource.Loading -> Resource.Loading
}

fun <T> Resource<T>.getOrNull(): T? = (this as? Resource.Success)?.data

fun <T> Resource<T>.isSuccess(): Boolean = this is Resource.Success