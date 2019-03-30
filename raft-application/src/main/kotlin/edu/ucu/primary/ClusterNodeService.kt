package edu.ucu.primary

import edu.ucu.proto.*
import edu.ucu.secondary.RaftHandler
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging


class ClusterNodeService(val raft: RaftHandler) : ClusterNodeGrpc.ClusterNodeImplBase() {

    private val logger = KotlinLogging.logger {}

    override fun requestVote(request: VoteRequest, responseObserver: StreamObserver<VoteResponse>) {

        val result = runBlocking { raft.requestVote(request) }
//        logger.info { "Vote request: $result" }
        responseObserver.onNext(result)
        responseObserver.onCompleted()
    }


    override fun appendEntries(request: AppendRequest, responseObserver: StreamObserver<AppendResponse>) {
//        logger.info { "🔨 Append Request: Leader: ${request.leaderId} Term ${request.term}" }
        val result = runBlocking {
            raft.appendEntries(request)
        }
        responseObserver.onNext(result)
        responseObserver.onCompleted()
    }
}
