package edu.ucu.raft.adapters

import edu.ucu.raft.adapters.RaftHandler

interface ClusterNode: RaftHandler {

    var nextIndex: Int
    var matchIndex: Int

    val nodeId: String

    fun decreaseIndex() {
        if (nextIndex > 0) {
            nextIndex -= 1
        }
    }

    fun reinitializeIndex(index: Int) {
        nextIndex = index
        matchIndex = 0
    }
}