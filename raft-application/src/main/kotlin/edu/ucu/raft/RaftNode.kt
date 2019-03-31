package edu.ucu.raft

import edu.ucu.raft.config.EnvConfiguration
import edu.ucu.raft.log.Command
import edu.ucu.raft.log.LogState
import edu.ucu.raft.grpc.ClusterNodeService
import edu.ucu.raft.grpc.GrpcClusterNode
import io.grpc.ServerBuilder

class RaftNode {

    val config = EnvConfiguration()
    val nodes = config.nodes
            .map { (id, host, port) -> GrpcClusterNode(host, port) }

    val controller = RaftController(config, nodes.toMutableList()).apply { start() }

    val server = ServerBuilder.forPort(config.port)
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