package edu.ucu.raft

import kotlinx.coroutines.delay


suspend fun wait(delayMs: Long = 50L, condition: () -> Boolean) {
    while (!condition()) {
        delay(delayMs)
    }
}