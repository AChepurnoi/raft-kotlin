package edu.ucu.secondary

import edu.ucu.proto.*
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import mu.KotlinLogging
import kotlin.coroutines.suspendCoroutine

class ClusterNode(val host: String, val port: Int) {
    private val logger = KotlinLogging.logger {}

    private val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build()
    private val stub = ClusterNodeGrpc.newBlockingStub(channel)


    suspend fun requestVote(candidateId: Int, term: Long): VoteResponse {
        val request = VoteRequest.newBuilder()
                .setTerm(term)
                .setCandidateId(candidateId)
                .build()

        val response = suspendCoroutine<VoteResponse> {
            val response = kotlin.runCatching { stub.requestVote(request)}.getOrElse {
                logger.error { "🤬 Vote request failed" }
                VoteResponse.newBuilder().setTerm(term).setVoteGranted(false).build()
            }

            it.resumeWith(Result.success(response))
        }
        return response
    }


    suspend fun sendHeartbeat(candidateId: Int, term: Long): AppendResponse {
        val request = AppendRequest.newBuilder().setTerm(term).setLeaderId(candidateId).build()
        val response = suspendCoroutine<AppendResponse> {
            val res = kotlin.runCatching { stub.appendEntries(request) }.getOrElse {
                logger.error { "🤬 Heartbeat failed" }
                AppendResponse.newBuilder().setSuccess(false).setTerm(term).build()
            }
            it.resumeWith(Result.success(res))
        }
        return response
    }

}

class Cluster {

    private val logger = KotlinLogging.logger {}

    private val nodes: MutableList<ClusterNode> = mutableListOf()
    fun add(node: ClusterNode) = nodes.add(node)


    suspend fun askVotes(candidateId: Int, term: Long): Boolean {
        if (nodes.isEmpty()) return false
        val majority = Math.floorDiv(nodes.size, 2) + 1

        val answers = Channel<VoteResponse>()
        val jobs = nodes.map {
            GlobalScope.launch {
                val result = it.requestVote(candidateId, term)
                answers.send(result)
            }
        }

        val voteResult = withTimeoutOrNull(200) {
            answers.filter { it.voteGranted }.take(majority).all { it.voteGranted }
        }

        return voteResult ?: kotlin.run {
            jobs.forEach { it.cancel() }
            return false
        }

    }

    suspend fun sendLeaderHeartbeat(candidateId: Int, term: Long): Boolean {
        nodes.forEach { it.sendHeartbeat(candidateId, term)}
        return true
    }

}