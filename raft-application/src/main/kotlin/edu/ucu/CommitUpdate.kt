package edu.ucu

import edu.ucu.secondary.ClusterNode
import mu.KotlinLogging

class CommitUpdate(val state: State, val cluster: List<ClusterNode>) {

    private val majority = Math.floorDiv(cluster.size, 2)
    private val log = state.log
    private val logger = KotlinLogging.logger {}


    fun perform() {
        if (state.current == NodeState.LEADER) {
            val newCommit = ((state.log.commitIndex + 1)..Int.MAX_VALUE)
                    .takeWhile { newCommit ->
                        val clusterApprove = matchIndexMatches(newCommit)
                        val logLastTermMatch = log[newCommit]?.term == state.term
                        logger.info { "Value: $newCommit - Cluster: $clusterApprove - LogLastTerm: $logLastTermMatch" }
                        clusterApprove && logLastTermMatch
                    }
                    .lastOrNull()
            logger.info { "Doing commit at $newCommit" }
            newCommit?.run { log.commit(this) }
        }
    }

    private fun matchIndexMatches(newCommit: Int) =
            cluster.filter { it.matchIndex >= newCommit }.count() > majority

}