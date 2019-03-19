package edu.ucu

import edu.ucu.primary.ClusterNodeService
import edu.ucu.secondary.Cluster
import edu.ucu.secondary.ClusterNode
import io.grpc.ServerBuilder
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

object Main {

    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        logger.info { ">>>>> Starting RAFT" }

        val clock = TermClock(5000)
        runBlocking { clock.start()}
        val cluster = Cluster().apply {
            Configuration.nodes
                    .map { (host, port) -> ClusterNode(host, port) }
                    .forEach { add(it) }
        }
        logger.info { "Cluster nodes: ${Configuration.nodes}" }
        val consensus = Consensus(clock, cluster)
//        clock.start()

        val server = ServerBuilder.forPort(Configuration.port).addService(ClusterNodeService(consensus)).build()
        server.start()

        server.awaitTermination()
        logger.info { ">>>>> Stopping RAFT" }

    }
}