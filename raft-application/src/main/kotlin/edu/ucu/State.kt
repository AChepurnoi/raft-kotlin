package edu.ucu

import edu.ucu.proto.AppendRequest
import edu.ucu.proto.VoteRequest
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
        internal set

    var current: NodeState = NodeState.FOLLOWER
        private set

    var votedFor: Int? = null

    var leaderId: Int? = null


    suspend fun nextTerm(newTerm: Long) {
        this.term = newTerm
        this.votedFor = id
        updateState(NodeState.CANDIDATE)
    }

    suspend fun promoteToLeader() {
        votedFor = null
        updateState(NodeState.LEADER)
    }

    suspend fun tryVote(request: VoteRequest): Boolean {

        return when {
            this.term > request.term -> false

            this.term < request.term -> {
                this.term = request.term
                this.votedFor = request.candidateId
                return true
            }
            this.term == request.term -> {
                if (this.votedFor == null && current != NodeState.LEADER) {
                    this.votedFor = request.candidateId
                }
                return votedFor == request.candidateId
            }
            else -> {
                logger.warn { "🤬 Voting failed: $request" }
                return false
            }
        }
    }

    suspend fun appendLeaderHeartbeat(request: AppendRequest): Boolean {
        return when {
            this.term <= request.term && request.leaderId != id -> {
                this.term = request.term
                updateState(NodeState.FOLLOWER)
                this.leaderId = request.leaderId
                this.votedFor = null
                return true
            }
            this.term > request.term -> false

            request.leaderId == id -> {
                this.leaderId = request.leaderId
                true
            }

            else -> {
                logger.warn { "🤬 Can't handle leader message" }
                return false
            }
        }
    }

    override fun toString(): String {
        return "State[id=$id, term=$term, current=$current, leaderId=$leaderId, votedFor=$votedFor]"
    }

}

