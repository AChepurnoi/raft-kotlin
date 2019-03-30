package edu.ucu.secondary

import edu.ucu.proto.*
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import mu.KotlinLogging
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class GrpcClusterNode(val host: String, val port: Int) : ClusterNode {

    private val logger = KotlinLogging.logger {}

    @Volatile
    override var nextIndex: Int = 0
    @Volatile
    override var matchIndex: Int = 0

    fun decreaseNextIndex() {
        if (nextIndex > 0) {
            nextIndex -= 1
        }
    }


    private val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build()
    private val stub = ClusterNodeGrpc.newBlockingStub(channel)


    override suspend fun requestVote(request: VoteRequest): VoteResponse? {
        return suspendCoroutine {
            it.resume(kotlin.runCatching { stub.requestVote(request) }.getOrNull())
        }
    }


    override suspend fun appendEntries(request: AppendRequest): AppendResponse? {
        return suspendCoroutine {
            it.resume(kotlin.runCatching { stub.appendEntries(request) }.getOrNull())
        }
    }

    fun reinitializeIndex(index: Int) {
        logger.info { "Index for node $host:$port reinitialized" }
        nextIndex = index
        matchIndex = 0
    }

}
