package edu.ucu.primary

import edu.ucu.Raft
import edu.ucu.proto.*
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging


class ClusterNodeService(val raft: Raft) : ClusterNodeGrpc.ClusterNodeImplBase() {

    private val logger = KotlinLogging.logger {}

    override fun requestVote(request: VoteRequest, responseObserver: StreamObserver<VoteResponse>) {

        val result = runBlocking { raft.grantVoteTo(request) }
//        logger.info { "Vote request: $result" }
        val response = VoteResponse.newBuilder().setVoteGranted(result).setTerm(raft.state.term).build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }


    override fun appendEntries(request: AppendRequest, responseObserver: StreamObserver<AppendResponse>) {
//        logger.info { "🔨 Append Request: Leader: ${request.leaderId} Term ${request.term}" }
        runBlocking {
            raft.validateLeaderMessage(request)
        }
        responseObserver.onNext(AppendResponse.getDefaultInstance())
        responseObserver.onCompleted()
    }
}
