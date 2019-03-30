package edu.ucu

import kotlinx.coroutines.delay


suspend fun wait(delayMs: Long = 50L, condition: () -> Boolean): Unit {
    while (!condition()) {
        delay(delayMs)
    }
}