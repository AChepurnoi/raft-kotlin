package edu.ucu.raft.actions

import edu.ucu.raft.state.State
import edu.ucu.raft.test
import edu.ucu.raft.testNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner

@RunWith(BlockJUnit4ClassRunner::class)
class VotingActionTest {


    @Test
    fun candidateCanGetVotes() = test {
        val state = State(1)
        val stateTwo = State(2)
        val stateThree = State(3)

        val voting = VotingAction(state, listOf(stateTwo.testNode(), stateThree.testNode()))
        state.nextTerm(2)
        val votingResult = voting.askVotes()

        assertThat(votingResult).isTrue()
    }


    @Test
    fun candidateWithStaleTermCantGetVotes() = test {
        val state = State(1, term = 0)
        val stateTwo = State(2, term = 2)
        val stateThree = State(3, term = 2)

        val voting = VotingAction(state, listOf(stateTwo.testNode(), stateThree.testNode()))

        state.nextTerm(1)
        val votingResult = voting.askVotes()

        assertThat(votingResult).isFalse()
    }
}