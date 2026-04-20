package com.oussama_chatri.productivityx.core.util

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val code: String? = null) : UiState<Nothing>()
}

fun <T> UiState<T>.dataOrNull(): T? = (this as? UiState.Success)?.data

fun <T> UiState<T>.isLoading(): Boolean = this is UiState.Loading

fun <T> UiState<T>.isError(): Boolean = this is UiState.Error