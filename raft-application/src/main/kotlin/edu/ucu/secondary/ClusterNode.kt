package edu.ucu.secondary

import edu.ucu.proto.AppendRequest
import edu.ucu.proto.AppendResponse
import edu.ucu.proto.VoteRequest
import edu.ucu.proto.VoteResponse

interface ClusterNode: RaftHandler{

    var nextIndex: Int
    var matchIndex: Int

    fun decreaseIndex() {
        if (nextIndex > 0) {
            nextIndex -= 1
        }
    }
}