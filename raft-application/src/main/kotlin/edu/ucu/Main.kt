package edu.ucu

import edu.ucu.primary.ClusterNodeService
import edu.ucu.secondary.GrpcClusterNode
import io.grpc.ServerBuilder
import mu.KotlinLogging

object Main {

    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        logger.info { ">>>>> Starting RAFT" }
        logger.info { "Cluster nodes: ${Configuration.nodes}" }
        val consensus = RaftNode()
        consensus.await()
        logger.info { ">>>>> Stopping RAFT" }

    }
}