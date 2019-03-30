package edu.ucu.state

import edu.ucu.NodeState
import edu.ucu.State
import edu.ucu.proto.AppendRequest
import edu.ucu.proto.LogEntry
import edu.ucu.proto.VoteRequest
import edu.ucu.test
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner

@RunWith(BlockJUnit4ClassRunner::class)
class StateTest {


    val state = State(5)
    val sub = state.updates.openSubscription()

    @Test
    fun nextTermConvertsFollowerToCandidate() = test {
        val prevTerm = state.term
        state.nextTerm(prevTerm + 1)
        assertThat(state.current).isEqualTo(NodeState.CANDIDATE)
        assertThat(state.term).isEqualTo(prevTerm + 1)
    }

    @Test
    fun stateChangesAreStreamedToChannel() = test {
        state.nextTerm(2)
        val (prev, next) = sub.receive()
        assertThat(state.current).isEqualTo(NodeState.CANDIDATE)
        assertThat(prev).isEqualTo(NodeState.FOLLOWER)
        assertThat(next).isEqualTo(NodeState.CANDIDATE)
    }



}