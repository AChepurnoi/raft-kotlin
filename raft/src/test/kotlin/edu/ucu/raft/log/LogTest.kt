package edu.ucu.raft.log

import edu.ucu.proto.LogEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner

@RunWith(BlockJUnit4ClassRunner::class)
class LogTest {


    @Test
    fun logCanAppend() {
        val log = Log()
        log.append(LogEntry.getDefaultInstance())
        log.append(LogEntry.getDefaultInstance())
        log.append(LogEntry.getDefaultInstance())

        assertThat(log.lastIndex()).isEqualTo(2)
    }

    @Test
    fun startingCanFetchLog() {
        val log = Log()
        log.append(LogEntry.getDefaultInstance())
        log.append(LogEntry.getDefaultInstance())
        log.append(LogEntry.getDefaultInstance())

        assertThat(log.starting(-1)).hasSize(3)
        assertThat(log.starting(0)).hasSize(3)
        assertThat(log.starting(1)).hasSize(2)
        assertThat(log.starting(2)).hasSize(1)
        assertThat(log.starting(3)).hasSize(0)
        assertThat(log.starting(4)).hasSize(0)
        assertThat(log.starting(5)).hasSize(0)
        assertThat(log.starting(6)).hasSize(0)
    }


    @Test
    fun commandCanBeSerializedAndDeserialized() {
        val set = Set("key", "value".toByteArray())

        val entry = LogEntry.newBuilder().setCommandBytes(set.toBytes()).build()

        val bytes = entry.commandBytes.toByteArray()
        val readed = Command.fromBytes(bytes)

        assertThat(readed).isInstanceOf(Set::class.java)
    }


    @Test
    fun logStateCanBeConstructed() {
        val log = Log()
        val set = Set("key", "value".toByteArray())
        val entry = LogEntry.newBuilder().setCommandBytes(set.toBytes()).build()
        log.append(entry)
        log.commit(0)
        val state = log.state()
        val s = String(state["key"]!!)
        assertThat(s).isEqualTo("value")

    }
}