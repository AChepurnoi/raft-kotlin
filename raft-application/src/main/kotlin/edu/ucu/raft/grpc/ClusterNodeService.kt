package edu.ucu.raft.grpc

import edu.ucu.proto.*
import edu.ucu.raft.adapters.RaftHandler
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging


class ClusterNodeService(val raft: RaftHandler) : ClusterNodeGrpc.ClusterNodeImplBase() {

    private val logger = KotlinLogging.logger {}

    override fun requestVote(request: VoteRequest, responseObserver: StreamObserver<VoteResponse>) {

        val result = runBlocking { raft.requestVote(request) }
        responseObserver.onNext(result)
        responseObserver.onCompleted()
    }


    override fun appendEntries(request: AppendRequest, responseObserver: StreamObserver<AppendResponse>) {
        val result = runBlocking {
            raft.appendEntries(request)
        }
        responseObserver.onNext(result)
        responseObserver.onCompleted()
    }
}
