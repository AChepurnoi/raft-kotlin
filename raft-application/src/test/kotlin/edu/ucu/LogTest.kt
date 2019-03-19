package edu.ucu

import edu.ucu.log.Log
import edu.ucu.log.Set
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner
import java.lang.RuntimeException
import java.util.*


@RunWith(BlockJUnit4ClassRunner::class)
class LogTest{

    @Test
    fun logCanIncreaseTerm() {
        val log = Log(currentTerm = 0).nextTerm()
        assert(log.currentTerm == 1L)
    }

    @Test
    fun logCanBeReconstructedAfterSet() {
        val log = Log(currentTerm = 0, committed = LinkedList(mutableListOf(Set("key", "value"))))
        val state = log.logState
        val value = state["key"] ?: throw RuntimeException("Failed")
        assert(value == "value")
    }


    @Test
    fun logCanBeCommitted() {
        val log = Log()
        val state = log.logState
        assert(state["key"] == null)

        log.append(Set("key", "value"))
        val updatedState = log.commit(state)
        assert(updatedState["key"]?.equals("value") ?: false)
    }

}