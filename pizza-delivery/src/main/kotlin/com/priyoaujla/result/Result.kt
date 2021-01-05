package com.priyoaujla.result

sealed class Result<out E, out T>

data class Success<out T>(val value: T) : Result<Nothing, T>()
data class Failure<out E>(val reason: E) : Result<E, Nothing>()

inline fun <T, Tʹ, E> Result<E, T>.map(f: (T) -> Tʹ): Result<E, Tʹ> =
    flatMap { value -> Success(f(value)) }

inline fun <T, Tʹ, E> Result<E, T>.flatMap(f: (T) -> Result<E, Tʹ>): Result<E, Tʹ> =
    when (this) {
        is Success<T> -> f(value)
        is Failure<E> -> this
    }