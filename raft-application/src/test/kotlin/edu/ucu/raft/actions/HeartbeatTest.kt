package edu.ucu.raft.actions

import edu.ucu.raft.log.Log
import edu.ucu.proto.LogEntry
import edu.ucu.raft.state.NodeState
import edu.ucu.raft.state.State
import edu.ucu.raft.test
import edu.ucu.raft.testNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner

@RunWith(BlockJUnit4ClassRunner::class)
class HeartbeatTest {

    @Test
    fun heartbeatFromLeaderIsAccepted() = test {
        val state = State(1, startState = NodeState.LEADER)
        val stateTwo = State(2, startState = NodeState.FOLLOWER)
        val stateThree = State(3, startState = NodeState.FOLLOWER)


        assertThat(stateTwo.leaderId).isNull()
        assertThat(stateThree.leaderId).isNull()

        val heartbeat = Heartbeat(state, listOf(stateTwo.testNode(), stateThree.testNode()))
        heartbeat.send()

        assertThat(stateTwo.leaderId).isEqualTo(1)
        assertThat(stateThree.leaderId).isEqualTo(1)

    }

    @Test
    fun logCanBeReplicatedToEmptyFollowers() = test {
        val log = Log().apply {
            append(LogEntry.newBuilder().build())
            append(LogEntry.newBuilder().build())
            append(LogEntry.newBuilder().build())
            commit(2)
        }

        val state = State(1, startState = NodeState.LEADER, log = log)
        val stateTwo = State(2, startState = NodeState.FOLLOWER)
        val stateThree = State(3, startState = NodeState.FOLLOWER)

        val heartbeat = Heartbeat(state, listOf(stateTwo.testNode(), stateThree.testNode()))
        heartbeat.send()
        assertThat(stateTwo.log.lastIndex()).isEqualTo(log.lastIndex())

    }

    @Test
    fun invalidLogOverwritten() = test {

        val state = State(1, startState = NodeState.LEADER, log = Log().apply {
            append(LogEntry.newBuilder().setTerm(1).build())
            append(LogEntry.newBuilder().setTerm(1).build())
            append(LogEntry.newBuilder().setTerm(2).build())
            commit(2)
        }
        )
        val stateTwo = State(2, startState = NodeState.FOLLOWER, log = Log().apply {
            append(LogEntry.newBuilder().setTerm(1).build())
            append(LogEntry.newBuilder().setTerm(1).build())
            append(LogEntry.newBuilder().setTerm(1).build())
            commit(2)
        }
        )
        val stateThree = State(3, startState = NodeState.FOLLOWER, log = Log().apply {
            append(LogEntry.newBuilder().setTerm(1).build())
            append(LogEntry.newBuilder().setTerm(1).build())
            append(LogEntry.newBuilder().setTerm(1).build())
            commit(2)
        })

        assertThat(stateTwo.log.lastTerm()).isEqualTo(1)

        val heartbeat = Heartbeat(state, listOf(stateTwo.testNode(), stateThree.testNode()))
        heartbeat.send()
        heartbeat.send()
        assertThat(stateTwo.log.lastTerm()).isEqualTo(2)

    }



}