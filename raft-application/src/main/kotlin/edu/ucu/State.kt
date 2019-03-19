package edu.ucu

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging


enum class NodeState {
    FOLLOWER, LEADER, CANDIDATE
}

class State(val id: Int = Configuration.id) {


    private val logger = KotlinLogging.logger {}
    val updates = ConflatedBroadcastChannel<Pair<NodeState, NodeState>>()

    private suspend fun updateState(next: NodeState) {
        logger.info { "🔴 State $current -> $next" }
        val prev = current
        current = next
        updates.send(prev to current)
    }

    var term: Long = 0
        private set

    var current: NodeState = NodeState.FOLLOWER
        private set

    var votedFor: Int? = null


    suspend fun nextTerm(newTerm: Long){
        this.term = newTerm
        this.votedFor = id
        updateState(NodeState.CANDIDATE)
    }

    suspend fun promoteToLeader() {
        votedFor = null
        updateState(NodeState.LEADER)
    }

    suspend fun tryVote(candidateId: Int, term: Long):Boolean{
        return when {
            this.term > term -> false

            this.term < term -> {
                this.term = term
                this.votedFor = candidateId
                return true
            }
            this.term == term -> {
                if (this.votedFor == null) {
                    this.votedFor = candidateId
                }
                return votedFor == candidateId
            }
            else -> {
                logger.warn { "🤬 Voting failed. $candidateId - $term" }
                return false
            }
        }
    }

    suspend fun appendLeaderHeartbeat(leaderId: Int, term: Long): Boolean {
        return when {
            this.term <= term && leaderId != id -> {
                this.term = term
                updateState(NodeState.FOLLOWER)
                this.votedFor = null
                return true
            }
            this.term > term -> false

            leaderId == id -> true

            else -> {
                logger.warn { "🤬 Can't handle leader message" }
                return false
            }
        }
    }

    override fun toString(): String {
        return "State[id=$id, term=$term, current=$current, votedFor=$votedFor]"
    }

}

