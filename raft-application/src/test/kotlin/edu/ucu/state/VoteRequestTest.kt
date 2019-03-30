package edu.ucu.state

import edu.ucu.State
import edu.ucu.proto.LogEntry
import edu.ucu.proto.VoteRequest
import edu.ucu.test
import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner


@RunWith(BlockJUnit4ClassRunner::class)
class VoteRequestTest {


    val state = State(5)

    @Test
    fun voteForCandidateWithSmallerTermIsNotGranted() = test {
        val request = VoteRequest.newBuilder()
                .setCandidateId(1).setTerm(-1).setLastLogIndex(-1).setLastLogTerm(-1)
                .build()
        val result = state.requestVote(request)

        assertThat(result?.voteGranted).isFalse()
        assertThat(state.votedFor).isNull()
    }


    @Test
    fun voteInSingleTermIsNotChanging() = test {
        val request = VoteRequest.newBuilder()
                .setCandidateId(1).setTerm(1).setLastLogIndex(-1).setLastLogTerm(-1)
                .build()

        val vote = state.requestVote(request)
        assertThat(vote?.voteGranted).isTrue()
        val requestTwo = VoteRequest.newBuilder()
                .setCandidateId(2).setTerm(1).setLastLogIndex(-1).setLastLogTerm(-1)
                .build()

        val voteTwo = state.requestVote(requestTwo)
        assertThat(voteTwo?.voteGranted).isFalse()
    }

    @Test
    fun voteForHigherTermIsGrantedEvenIfVotedBefore() = test {
        val vote = state.requestVote(VoteRequest.newBuilder()
                .setCandidateId(1).setTerm(1).setLastLogIndex(-1).setLastLogTerm(-1)
                .build())
        assertThat(vote?.voteGranted).isTrue()
        val secondVote = state.requestVote(VoteRequest.newBuilder()
                .setCandidateId(2).setTerm(2).setLastLogIndex(-1).setLastLogTerm(-1)
                .build())
        assertThat(secondVote?.voteGranted).isTrue()

        assertThat(state.term).isEqualTo(2)
        assertThat(state.votedFor).isEqualTo(2)

    }

    @Test
    fun voteForCandidateWithBiggerTermAndShorterLogIsGranted() = test {
        state.log[0] = LogEntry.newBuilder().setTerm(0).build()
        state.log[1] = LogEntry.newBuilder().setTerm(0).build()
        state.log[2] = LogEntry.newBuilder().setTerm(0).build()

        val vote = state.requestVote(VoteRequest.newBuilder()
                .setCandidateId(1).setTerm(1).setLastLogIndex(1).setLastLogTerm(1)
                .build())

        assertThat(vote?.voteGranted).isTrue()
        assertThat(state.votedFor).isEqualTo(1)
    }

    @Test
    fun voteForCandidateWithSmallerTermNotGranted() = test {
        state.log[0] = LogEntry.newBuilder().setTerm(0).build()
        state.log[1] = LogEntry.newBuilder().setTerm(0).build()
        state.log[2] = LogEntry.newBuilder().setTerm(1).build()

        val vote = state.requestVote(VoteRequest.newBuilder()
                .setCandidateId(1).setTerm(1).setLastLogIndex(2).setLastLogTerm(0)
                .build())

        assertThat(vote?.voteGranted).isFalse()
        assertThat(state.votedFor).isNull()
    }


}