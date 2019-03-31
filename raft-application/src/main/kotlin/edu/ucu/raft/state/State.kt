package edu.ucu.raft.state

import edu.ucu.raft.log.Command
import edu.ucu.raft.log.Log
import edu.ucu.proto.*
import edu.ucu.raft.config.EnvConfiguration
import edu.ucu.raft.wait
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.delay
import mu.KotlinLogging

class State(val id: Int,
            val log: Log = Log(),
            startState: NodeState = NodeState.FOLLOWER,
            term: Long = 0) {

    private val logger = KotlinLogging.logger {}
    val updates = ConflatedBroadcastChannel<Pair<NodeState, NodeState>>()

    var term: Long = term
        internal set

    @Volatile
    var current: NodeState = startState
        private set

    @Volatile
    var votedFor: Int? = null

    @Volatile
    var leaderId: Int? = null

    private suspend fun updateState(next: NodeState) {
        if (next == current) return
        logger.info { "🔴 State $current -> $next" }
        val prev = current
        current = next
        updates.send(prev to current)
    }

    suspend fun nextTerm(newTerm: Long) {
        this.term = newTerm
        this.votedFor = id
        updateState(NodeState.CANDIDATE)
    }

    suspend fun promoteToLeader() {
        votedFor = null
        updateState(NodeState.LEADER)
    }

    suspend fun requestVote(request: VoteRequest): VoteResponse {

        if (request.term > this.term) {
            this.term = term
            updateState(NodeState.FOLLOWER)
        }

        val logValid = validateLog(request)

        val voteResult = when {
            this.term > request.term || !logValid -> false

            this.term < request.term -> {
                this.term = request.term
                this.votedFor = request.candidateId
                updateState(NodeState.FOLLOWER)
                true
            }
            this.term == request.term -> {
                if (this.votedFor == null && current != NodeState.LEADER) {
                    this.votedFor = request.candidateId
                }
                votedFor == request.candidateId
            }
            else -> {
                logger.warn { "🤬 Voting failed: $request" }
                false
            }
        }

        return VoteResponse.newBuilder().setTerm(term).setVoteGranted(voteResult).build()
    }

    private fun validateLog(request: VoteRequest): Boolean {

        val leaderLogLastTermIsBigger = log.lastTerm()?.let { it < request.lastLogTerm } ?: false
        if (leaderLogLastTermIsBigger) return true


        val leaderLogLastTermEqual = log.lastTerm()?.let { it == request.lastLogTerm } ?: true

        val leaderLogLongerOrEqual = log.lastIndex() <= request.lastLogIndex

        return leaderLogLastTermEqual && leaderLogLongerOrEqual
    }

    suspend fun appendEntries(request: AppendRequest): AppendResponse {
        if (request.term < this.term) return AppendResponse.newBuilder().setTerm(term).setSuccess(false).build()
        if (request.term >= this.term) {
            this.term = request.term
            this.votedFor = null
            this.leaderId = request.leaderId
            updateState(NodeState.FOLLOWER)
        }

        if (request.prevLogIndex != -1) {
            val logValid = log[request.prevLogIndex]?.run { this.term == request.prevLogTerm } ?: false
            if (!logValid) {
                logger.warn { "Master log is not cool" }
                return AppendResponse.newBuilder().setTerm(term).setSuccess(false).build()
            }
        }

        var idx = request.prevLogIndex + 1
        for (entry in request.entriesList) {

            if (log.isNotEmpty()) {
                val logConflict = log[idx]?.run { this.term != entry.term } ?: false
                if (logConflict) {
                    log.prune(idx)
                }
            }
            log[idx] = entry
            idx += 1
        }

        this.leaderId = request.leaderId
        log.commit(request.leaderCommit)
        return AppendResponse.newBuilder().setTerm(term).setSuccess(true).build()

    }

    override fun toString(): String {
        return "State[id=$id, term=$term, current=$current, leaderId=$leaderId, votedFor=$votedFor, log=$log]"
    }

    suspend fun applyCommand(command: Command): Boolean {
        val index = log.append(LogEntry.newBuilder().setTerm(term).setCommandBytes(command.toBytes()).build())
        while (index > log.commitIndex){
            delay(50)
        }
        return true
    }

}

