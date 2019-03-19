package edu.ucu

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner

@RunWith(BlockJUnit4ClassRunner::class)
class TermClockTest {


    @Test(timeout = 10000)
    fun clockTest() {
        runBlocking {
            val clock = TermClock(1000)
            clock.start()
            val subscription = clock.channel.openSubscription()

            val term = subscription.receive()
            println("Recieved $term")
            assertThat(term).isEqualTo(1L)

            clock.freeze()

            val result = withTimeoutOrNull(2000) {
                subscription.receive()
            }
            assertThat(result).isNull()
            clock.start()

            val termTwo = subscription.receive()
            println("Recieved $termTwo")
            assertThat(termTwo).isEqualTo(2L)


            val termThree = subscription.receive()
            println("Recieved $termThree")
            assertThat(termThree).isEqualTo(3L)

        }

    }
}