package edu.ucu.raft.clock

//import mu.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.fixedRateTimer

class TermClock(private val interval: Long) {
    private val logger = KotlinLogging.logger {}


    val channel = ConflatedBroadcastChannel<Long>()
    var term: Long = 0
        private set

    private val mutex: Mutex = Mutex(false)
    private var stopped = true
    private lateinit var timer: Timer

    private suspend fun updateTerm(term: Long) {
        logger.info { "⏳ Term ${this.term} -> $term" }
        this.term = term
        channel.send(term)
    }

    suspend fun start() {
        mutex.withLock {
            if (stopped) {
                stopped = false
                schedule()
            }
        }
    }

    private fun schedule() {
        timer = fixedRateTimer(initialDelay = interval, period = interval) {
            runBlocking { updateTerm(term + 1) }
        }
    }

    suspend fun update(newTerm: Long) {
        mutex.withLock{
            this.term = newTerm
        }
    }

    suspend fun reset() {
        mutex.withLock {
            if (stopped) return
            timer.cancel()
            schedule()
        }

    }

    suspend fun freeze() {
        mutex.withLock {
            if (!stopped) {
                stopped = true
                timer.cancel()
            }
        }
    }
}