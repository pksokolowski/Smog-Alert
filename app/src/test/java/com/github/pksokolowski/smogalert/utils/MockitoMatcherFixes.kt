package com.github.pksokolowski.smogalert.utils

import org.mockito.Mockito


// fix for Mockito's bug with Kotlin's null safety
// this replaces argument matcher "any()"
fun <T> anything(): T {
    Mockito.any<T>()
    return uninitialized()
}

private fun <T> uninitialized(): T = null as T