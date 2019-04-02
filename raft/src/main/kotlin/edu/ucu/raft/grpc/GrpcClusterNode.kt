package edu.ucu.raft.grpc

import edu.ucu.proto.*
import edu.ucu.raft.adapters.ClusterNode
import io.grpc.ManagedChannelBuilder
import mu.KotlinLogging
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class GrpcClusterNode(val host: String, val port: Int) : ClusterNode {

    private val logger = KotlinLogging.logger {}

    @Volatile
    override var nextIndex: Int = 0
    @Volatile
    override var matchIndex: Int = -1

    override val nodeId: String = "$host:$port"

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

    override fun reinitializeIndex(index: Int) {
        logger.info { "Index for node $host:$port reinitialized" }
        super.reinitializeIndex(index)
    }

}
