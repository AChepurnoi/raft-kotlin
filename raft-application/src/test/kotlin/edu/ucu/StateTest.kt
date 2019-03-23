package edu.ucu

import edu.ucu.proto.VoteRequest
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner

@RunWith(BlockJUnit4ClassRunner::class)
class StateTest {


    val state = State(5)
    val sub = state.updates.openSubscription()

    @Test
    fun nextTermConvertsFollowerToCandidate() {
        runBlocking {
            val prevTerm = state.term
            state.nextTerm(prevTerm + 1)

            val (prev, next) = sub.receive()
            assertThat(state.current).isEqualTo(NodeState.CANDIDATE)
            assertThat(state.term).isEqualTo(prevTerm + 1)

        }
    }

    @Test
    fun stateChangesAreStreamedToChannel() {
        runBlocking {
            state.nextTerm(2)
            val (prev, next) = sub.receive()
            assertThat(state.current).isEqualTo(NodeState.CANDIDATE)
            assertThat(prev).isEqualTo(NodeState.FOLLOWER)
            assertThat(next).isEqualTo(NodeState.CANDIDATE)

        }
    }

    @Test
    fun voteForCandidateWithSmallerTermIsNotGranted() {
        runBlocking {
            val request = VoteRequest.newBuilder().setCandidateId(1).setTerm(-1).build()
            state.tryVote(request)
            assertThat(state.votedFor).isNull()
        }
    }


    @Test
    fun voteInOneTermIsNotChaning() {
        runBlocking {
            val request = VoteRequest.newBuilder().setCandidateId(1).setTerm(1).build()

            val vote = state.tryVote(request)
            assertThat(vote).isTrue()
            val requestTwo = VoteRequest.newBuilder().setCandidateId(2).setTerm(1).build()

            val voteTwo = state.tryVote(requestTwo)
            assertThat(voteTwo).isFalse()
        }
    }
}