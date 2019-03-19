package edu.ucu.primary

import edu.ucu.Consensus
import edu.ucu.proto.*
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging


class ClusterNodeService(val consensus: Consensus) : ClusterNodeGrpc.ClusterNodeImplBase() {

    private val logger = KotlinLogging.logger {}

    override fun requestVote(request: VoteRequest, responseObserver: StreamObserver<VoteResponse>) {

        val result = runBlocking { consensus.grantVoteTo(request.candidateID, request.term) }
//        logger.info { "Vote request: $result" }
        val response = VoteResponse.newBuilder().setVoteGranted(result).setTerm(consensus.state.term).build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }


    override fun appendEntries(request: AppendRequest, responseObserver: StreamObserver<AppendResponse>) {
//        logger.info { "🔨 Append Request: Leader: ${request.leaderId} Term ${request.term}" }
        runBlocking {
            consensus.validateLeaderMessage(request.leaderId, request.term)
        }
        responseObserver.onNext(AppendResponse.getDefaultInstance())
        responseObserver.onCompleted()
    }
}
