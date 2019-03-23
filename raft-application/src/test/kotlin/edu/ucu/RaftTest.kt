package edu.ucu

import edu.ucu.proto.VoteRequest
import edu.ucu.secondary.Cluster
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.*
import org.mockito.Mockito.mock

@RunWith(BlockJUnit4ClassRunner::class)
class RaftTest {


    val channelMock = ConflatedBroadcastChannel<Long>()


    val clock = mock(TermClock::class.java).also {
        given(it.channel).willReturn(channelMock)
    }
    val cluster = mock(Cluster::class.java)

    val consensus = Raft(clock, cluster)


    @Test
    fun nodeBecomeACandidateAfterTermPassed() {
        assertThat(consensus.state.current).isEqualTo(NodeState.FOLLOWER)
        runBlocking {
            given(cluster.askVotes(ArgumentMatchers.anyInt(), ArgumentMatchers.anyLong()))
                    .willReturn(false)
        }
        val nextTerm = consensus.state.term + 1
        runBlocking {
            channelMock.send(nextTerm)
            delay(50)
        }
        assertThat(consensus.state.current).isEqualTo(NodeState.CANDIDATE)
        assertThat(consensus.state.term == nextTerm)
    }


    @Test
    fun nodeBecomeALeaderAfterMajorityVote() {
        runBlocking {
            given(cluster.askVotes(ArgumentMatchers.anyInt(), ArgumentMatchers.anyLong()))
                    .willReturn(true)
        }
        assertThat(consensus.state.current).isEqualTo(NodeState.FOLLOWER)
        val nextTerm = consensus.state.term + 1
        runBlocking {
            channelMock.send(nextTerm)
            delay(50)
        }
        assertThat(consensus.state.current).isEqualTo(NodeState.LEADER)
        assertThat(consensus.state.term == nextTerm)

    }

    @Test
    fun nodeCanVoteForCandidate() {
        assertThat(consensus.state.current).isEqualTo(NodeState.FOLLOWER)
        val request = VoteRequest.newBuilder().setCandidateId(10).setTerm(consensus.state.term).build()

        val result = runBlocking { consensus.grantVoteTo(request)}
        assertThat(result).isEqualTo(true)
        assertThat(consensus.state.votedFor).isEqualTo(10L)
    }

    @Test
    fun voteForLowerTermIsNotGranted() {
        assertThat(consensus.state.votedFor).isEqualTo(null)
        assertThat(consensus.state.current).isEqualTo(NodeState.FOLLOWER)
        val request = VoteRequest.newBuilder().setCandidateId(10).setTerm(consensus.state.term - 1).build()
        val result = runBlocking { consensus.grantVoteTo(request)}
        assertThat(result).isEqualTo(false)
        assertThat(consensus.state.votedFor).isEqualTo(null)
    }

    @Test
    fun onlyOneVotePerTermIsGranted() {
        assertThat(consensus.state.votedFor).isEqualTo(null)
        assertThat(consensus.state.current).isEqualTo(NodeState.FOLLOWER)

        val request = VoteRequest.newBuilder().setCandidateId(10).setTerm(consensus.state.term ).build()

        val result = runBlocking { consensus.grantVoteTo(request)}

        assertThat(result).isEqualTo(true)
        assertThat(consensus.state.votedFor).isEqualTo(10L)

        val requestTwo = VoteRequest.newBuilder().setCandidateId(1).setTerm(consensus.state.term ).build()

        val resultTwo = runBlocking { consensus.grantVoteTo(requestTwo)}

        assertThat(resultTwo).isEqualTo(false)
        assertThat(consensus.state.votedFor).isEqualTo(10)
    }

}