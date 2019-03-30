package edu.ucu

import edu.ucu.proto.VoteRequest
import edu.ucu.proto.VoteResponse
import edu.ucu.secondary.ClusterNode
import edu.ucu.secondary.GrpcClusterNode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeoutOrNull

class Voting(val state: State, val cluster: List<ClusterNode>) {

    private val majority = Math.floorDiv(cluster.size, 2)


    private fun checkTerm(response: VoteResponse) {
        if (response.term > this.state.term) {

        }
    }

    suspend fun askVotes(): Boolean {
        val request = VoteRequest.newBuilder().setTerm(state.term).setCandidateId(state.id)
                .setLastLogIndex(state.log.lastIndex())
                .setLastLogTerm(state.log.lastTerm() ?: -1).build()

        val responses = cluster.map { node -> GlobalScope.async { node.requestVote(request) } }
                .map { withTimeoutOrNull(200) { it.await() } }
                .filterNotNull()

        responses.forEach { checkTerm(it) }
        val votes = responses.filter { it.voteGranted }.count()

        return votes >= majority

    }
}