package edu.ucu.state

import edu.ucu.NodeState
import edu.ucu.State
import edu.ucu.proto.AppendRequest
import edu.ucu.proto.LogEntry
import edu.ucu.test
import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner

@RunWith(BlockJUnit4ClassRunner::class)
class AppendRequestTest {

    val state = State(5)

    @Test
    fun logEntryCouldBeAppendedToEmptyLog() = test {
        val request = buildAppendRequest()
        assertThat(state.log.lastIndex()).isEqualTo(-1)

        val result = state.appendEntries(request)
        assertThat(state.log.lastIndex()).isEqualTo(0)
        assertThat(result?.success).isTrue()
    }


    @Test
    fun logEntryCouldBeAppliedSequentially() = test {
        val request = buildAppendRequest()
        assertThat(state.log.lastIndex()).isEqualTo(-1)

        val result = state.appendEntries(request)
        assertThat(state.log.lastIndex()).isEqualTo(0)

        val rTwo = buildAppendRequest(prevLogIndex = 0, prevLogTerm = 0)
        val resultTwo = state.appendEntries(rTwo)

        assertThat(state.log.lastIndex()).isEqualTo(1)
        assertThat(resultTwo?.success).isTrue()
    }

    @Test
    fun prevTermValidationIsWorking() = test {
        val request = buildAppendRequest()
        val result = state.appendEntries(request)
        assertThat(state.log.lastIndex()).isEqualTo(0)

        val rTwo = buildAppendRequest(prevLogIndex = 0, prevLogTerm = 1)
        val resultTwo = state.appendEntries(rTwo)

        assertThat(state.log.lastIndex()).isEqualTo(0)
        assertThat(resultTwo?.success).isFalse()
    }

    @Test
    fun prevLogIndexValidationIsWorking() = test {
        val request = buildAppendRequest()
        val result = state.appendEntries(request)
        assertThat(state.log.lastIndex()).isEqualTo(0)

        val rTwo = buildAppendRequest(prevLogIndex = 1, prevLogTerm = 0)
        val resultTwo = state.appendEntries(rTwo)
        assertThat(state.log.lastIndex()).isEqualTo(0)
        assertThat(resultTwo?.success).isFalse()
    }


    @Test
    fun expectSameEntryApplicationOverrided() = test {
        val request = buildAppendRequest()
        assertThat(state.log.lastIndex()).isEqualTo(-1)

        val result = state.appendEntries(request)
        assertThat(state.log.lastIndex()).isEqualTo(0)

        val resultTwo = state.appendEntries(request)

        assertThat(state.log.lastIndex()).isEqualTo(0)
        assertThat(resultTwo?.success).isTrue()
    }


    @Test
    fun expectOutdatedRequestTermIsDetected() = test {
        val request = buildAppendRequest(currentTerm = -1)
        assertThat(state.log.lastIndex()).isEqualTo(-1)

        val result = state.appendEntries(request)
        assertThat(state.log.lastIndex()).isEqualTo(-1)
        assertThat(result?.success).isFalse()
    }


    @Test
    fun expectConflictingLogEntryWillOverwriteExisting() = test {
        state.appendEntries(buildAppendRequest())
        state.appendEntries(buildAppendRequest(prevLogTerm = 0, prevLogIndex = 0))
        state.appendEntries(buildAppendRequest(prevLogTerm = 0, prevLogIndex = 1))

        assertThat(state.log.lastIndex()).isEqualTo(2)

        state.appendEntries(buildAppendRequest(prevLogTerm = 0, prevLogIndex = 0, entryTerm = 1))

        assertThat(state.log.lastIndex()).isEqualTo(1)
    }

    @Test
    fun expectNonConflictingLogEntryWillNotOverwriteExisting() = test {
        state.appendEntries(buildAppendRequest())
        state.appendEntries(buildAppendRequest(prevLogTerm = 0, prevLogIndex = 0))
        state.appendEntries(buildAppendRequest(prevLogTerm = 0, prevLogIndex = 1))

        assertThat(state.log.lastIndex()).isEqualTo(2)

        state.appendEntries(buildAppendRequest(prevLogTerm = 0, prevLogIndex = 0))

        assertThat(state.log.lastIndex()).isEqualTo(2)
    }


    @Test
    fun expectCommitIndexUpdatesToLeaders() = test {
        state.appendEntries(buildAppendRequest())
        state.appendEntries(buildAppendRequest(prevLogTerm = 0, prevLogIndex = 0))
        state.appendEntries(buildAppendRequest(prevLogTerm = 0, prevLogIndex = 1, leaderCommit = 1))


        assertThat(state.log.lastIndex()).isEqualTo(2)
        assertThat(state.log.commitIndex).isEqualTo(1)
    }


    @Test
    fun expectCommitIndexUpdatesAccordingToTheRulesOfMinBetweenLeaderAndLog() = test {
        state.appendEntries(buildAppendRequest())
        state.appendEntries(buildAppendRequest(prevLogTerm = 0, prevLogIndex = 0))
        state.appendEntries(buildAppendRequest(prevLogTerm = 0, prevLogIndex = 1, leaderCommit = 4))


        assertThat(state.log.lastIndex()).isEqualTo(2)
        assertThat(state.log.commitIndex).isEqualTo(2)
    }


    @Test
    fun expectHeartbeatCanBeProcessed() = test {
        state.appendEntries(heartbeatRequest(leaderId = 1))

        assertThat(state.leaderId).isEqualTo(1)
        assertThat(state.current).isEqualTo(NodeState.FOLLOWER)
    }


    @Test
    fun expectAppendMessageWithHigherTermUpdatesTerm() = test{
        state.appendEntries(heartbeatRequest(leaderId = 1, currentTerm = 5))

        assertThat(state.term).isEqualTo(5)
        assertThat(state.leaderId).isEqualTo(1)

    }

    @Test
    fun expectAppendMessageWithHigherTermConvertsToFollower() = test {
        state.nextTerm(1)
        assertThat(state.current).isEqualTo(NodeState.CANDIDATE)

        state.appendEntries(heartbeatRequest(leaderId = 1, currentTerm = 2))

        assertThat(state.term).isEqualTo(2)
        assertThat(state.leaderId).isEqualTo(1)
        assertThat(state.current).isEqualTo(NodeState.FOLLOWER)

    }

    fun buildAppendRequest(prevLogTerm: Long = -1,
                           prevLogIndex: Int = -1,
                           entryTerm: Long = 0,
                           currentTerm: Long = 0,
                           leaderId: Int = 1,
                           leaderCommit: Int = 0): AppendRequest {
        val entry = LogEntry.newBuilder().setTerm(entryTerm).build()
        val request = AppendRequest.newBuilder()
                .setTerm(currentTerm)
                .setLeaderId(leaderId)
                .setLeaderCommit(leaderCommit)
                .setPrevLogIndex(prevLogIndex)
                .setPrevLogTerm(prevLogTerm)
                .addEntries(entry)
                .build()
        return request
    }

    fun heartbeatRequest(prevLogTerm: Long = -1,
                         prevLogIndex: Int = -1,
                         currentTerm: Long = 0,
                         leaderId: Int = 1,
                         leaderCommit: Int = 0): AppendRequest {

        return AppendRequest.newBuilder()
                .setTerm(currentTerm)
                .setLeaderId(leaderId)
                .setLeaderCommit(leaderCommit)
                .setPrevLogIndex(prevLogIndex)
                .setPrevLogTerm(prevLogTerm)
                .build()
    }

}