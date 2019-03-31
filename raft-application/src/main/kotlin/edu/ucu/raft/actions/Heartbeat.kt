package edu.ucu.raft.actions

import edu.ucu.proto.AppendRequest
import edu.ucu.raft.adapters.ClusterNode
import edu.ucu.raft.state.NodeState
import edu.ucu.raft.state.State
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeoutOrNull
import mu.KotlinLogging
import java.lang.RuntimeException

class Heartbeat(val state: State, val cluster: List<ClusterNode>) {

    private val logger = KotlinLogging.logger {}

    private val log = state.log

    suspend fun send() {
        if (state.current == NodeState.LEADER) {
            cluster.map {
                val prevIndex = it.nextIndex - 1
                val prevTerm = if (prevIndex != -1) log[prevIndex]?.term ?: throw RuntimeException("WAT") else -1

                val entries = log.starting(prevIndex + 1)
                val request = AppendRequest.newBuilder()
                        .setTerm(state.term).setLeaderId(state.id).setLeaderCommit(state.log.commitIndex)
                        .setPrevLogIndex(prevIndex).setPrevLogTerm(prevTerm)
                        .addAllEntries(entries)
                        .build()


                GlobalScope.async {
                    val response = it.appendEntries(request)
                    if (response == null) null else it to response
                }
            }.map { withTimeoutOrNull(200) { it.await() } }
                    .filterNotNull()
                    .forEach { (node, response) ->
                        when {
                            response.success -> {
                                node.nextIndex = log.lastIndex() + 1
                                node.matchIndex = node.nextIndex - 1
                            }
                            !response.success -> {
                                logger.info { "Heartbeat response: ${response.success}-${response.term}" }
                                node.decreaseIndex()
                            }
                        }
                    }


        }
    }

}