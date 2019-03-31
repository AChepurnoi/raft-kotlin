package edu.ucu.raft

import edu.ucu.proto.VoteRequest
import edu.ucu.raft.adapters.ClusterNode
import edu.ucu.raft.clock.TermClock
import edu.ucu.raft.config.StaticConfiguration
import edu.ucu.raft.state.NodeState
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner
import org.mockito.BDDMockito.*
import org.mockito.Mockito.mock

@RunWith(BlockJUnit4ClassRunner::class)
class RaftControllerTest {


    val channelMock = ConflatedBroadcastChannel<Long>()


    val clock = mock(TermClock::class.java).also {
        given(it.channel).willReturn(channelMock)
    }
    val consensus = RaftController(StaticConfiguration(id = 5), mutableListOf())


    @Test
    fun controllerIsWorkingConsistently() = test {
        val raft = RaftController(StaticConfiguration(id = 1, heartbeatInterval = 50, timerInterval = 400))
        assertThat(raft.state.term).isEqualTo(0)
        delay(1000)
        assertThat(raft.state.term).isEqualTo(0)
        raft.start()
        delay(1000)
        raft.stop()
        assertThat(raft.state.term).isEqualTo(2)
        delay(1000)
        assertThat(raft.state.term).isEqualTo(2)
    }


    @Test
    fun nodeCanVoteForCandidate() {
        assertThat(consensus.state.current).isEqualTo(NodeState.FOLLOWER)
        val request = VoteRequest.newBuilder().setCandidateId(10).setTerm(consensus.state.term).build()

        val result = runBlocking { consensus.requestVote(request) }
        assertThat(result.voteGranted).isEqualTo(true)
        assertThat(consensus.state.votedFor).isEqualTo(10L)
    }

    @Test
    fun voteForLowerTermIsNotGranted() {
        assertThat(consensus.state.votedFor).isEqualTo(null)
        assertThat(consensus.state.current).isEqualTo(NodeState.FOLLOWER)
        val request = VoteRequest.newBuilder().setCandidateId(10).setTerm(consensus.state.term - 1).build()
        val result = runBlocking { consensus.requestVote(request) }
        assertThat(result.voteGranted).isEqualTo(false)
        assertThat(consensus.state.votedFor).isEqualTo(null)
    }

    @Test
    fun onlyOneVotePerTermIsGranted() {
        assertThat(consensus.state.votedFor).isEqualTo(null)
        assertThat(consensus.state.current).isEqualTo(NodeState.FOLLOWER)

        val request = VoteRequest.newBuilder().setCandidateId(10).setTerm(consensus.state.term).build()

        val result = runBlocking { consensus.requestVote(request) }

        assertThat(result.voteGranted).isEqualTo(true)
        assertThat(consensus.state.votedFor).isEqualTo(10L)

        val requestTwo = VoteRequest.newBuilder().setCandidateId(1).setTerm(consensus.state.term).build()

        val resultTwo = runBlocking { consensus.requestVote(requestTwo) }

        assertThat(resultTwo.voteGranted).isEqualTo(false)
        assertThat(consensus.state.votedFor).isEqualTo(10)
    }

}