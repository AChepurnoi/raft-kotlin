package edu.ucu

import kotlinx.coroutines.runBlocking


fun test(body: suspend () -> Unit) {
    runBlocking {
        body()
    }
}