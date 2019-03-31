package edu.ucu.raft.actions

import edu.ucu.raft.log.Log
import edu.ucu.proto.LogEntry
import edu.ucu.raft.state.NodeState
import edu.ucu.raft.state.State
import edu.ucu.raft.testNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner


@RunWith(BlockJUnit4ClassRunner::class)
class CommitTest {


    @Test
    fun logIsCommited() {
        val log = Log().apply {
            append(LogEntry.newBuilder().setTerm(0).build())
            append(LogEntry.newBuilder().setTerm(0).build())
            append(LogEntry.newBuilder().setTerm(0).build())

        }
        val state = State(id = 1, log = log, startState = NodeState.LEADER)
        val stateTwo = State(id = 1, log = log, startState = NodeState.FOLLOWER)
        val stateThree = State(id = 1, log = log, startState = NodeState.FOLLOWER)

        val commit = CommitUpdate(state,
                listOf(stateTwo.testNode().also { it.matchIndex = state.log.lastIndex() },
                        stateThree.testNode().also { it.matchIndex = state.log.lastIndex() }))
        assertThat(state.log.commitIndex).isEqualTo(-1)
        commit.perform()

        assertThat(state.log.commitIndex).isEqualTo(2)
    }

    @Test
    fun logCannotBeCommitedIfMajorityLogNotMatched() {
        val log = Log().apply {
            append(LogEntry.newBuilder().setTerm(0).build())
            append(LogEntry.newBuilder().setTerm(0).build())
            append(LogEntry.newBuilder().setTerm(0).build())

        }
        val state = State(id = 1, log = log, startState = NodeState.LEADER)
        val stateTwo = State(id = 1, log = Log(), startState = NodeState.FOLLOWER)
        val stateThree = State(id = 1, log = Log(), startState = NodeState.FOLLOWER)

        val commit = CommitUpdate(state, listOf(stateTwo.testNode(), stateThree.testNode()))
        assertThat(state.log.commitIndex).isEqualTo(-1)
        commit.perform()

        assertThat(state.log.commitIndex).isEqualTo(-1)
    }

    @Test
    fun logCannotBeCommitedIfTermNotMatched() {
        val log = Log().apply {
            append(LogEntry.newBuilder().setTerm(0).build())
            append(LogEntry.newBuilder().setTerm(0).build())
            append(LogEntry.newBuilder().setTerm(0).build())

        }
        val state = State(id = 1, log = log, startState = NodeState.LEADER, term = 1)
        val stateTwo = State(id = 1, log = Log(), startState = NodeState.FOLLOWER, term = 1)
        val stateThree = State(id = 1, log = Log(), startState = NodeState.FOLLOWER, term = 1)

        val commit = CommitUpdate(state, listOf(stateTwo.testNode(), stateThree.testNode()))
        assertThat(state.log.commitIndex).isEqualTo(-1)
        commit.perform()

        assertThat(state.log.commitIndex).isEqualTo(-1)
    }


}