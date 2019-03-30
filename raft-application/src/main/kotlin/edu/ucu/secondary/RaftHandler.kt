package edu.ucu.secondary

import edu.ucu.proto.AppendRequest
import edu.ucu.proto.AppendResponse
import edu.ucu.proto.VoteRequest
import edu.ucu.proto.VoteResponse

interface RaftHandler {
    suspend fun requestVote(request: VoteRequest): VoteResponse?

    suspend fun appendEntries(request: AppendRequest): AppendResponse?
}