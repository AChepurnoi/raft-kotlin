package edu.ucu

import edu.ucu.log.Command
import edu.ucu.log.LogState
import edu.ucu.primary.ClusterNodeService
import edu.ucu.secondary.GrpcClusterNode
import io.grpc.ServerBuilder

class RaftNode {

    val nodes = Configuration.nodes
            .map { (host, port) -> GrpcClusterNode(host, port) }
    val controller = RaftController(nodes).apply { start() }

    val server = ServerBuilder.forPort(Configuration.port)
            .addService(ClusterNodeService(controller))
            .build().apply { start() }

    fun getState(): LogState {
        return controller.state.log.state()
    }

    suspend fun applyCommand(command: Command): Boolean {
        return controller.applyCommand(command)
    }

    fun await() {
        server.awaitTermination()
    }


}