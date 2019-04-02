package edu.ucu.raft

import edu.ucu.raft.log.Command
import edu.ucu.raft.log.LogState
import edu.ucu.raft.grpc.ClusterNodeService
import edu.ucu.raft.grpc.GrpcClusterNode
import edu.ucu.raft.state.NodeState
import io.grpc.ServerBuilder
import java.lang.RuntimeException

class RaftNode {

    val config = EnvConfiguration()
    private val nodes = config.nodes
            .map { (id, host, port) -> GrpcClusterNode(host, port) }

    private val controller = RaftController(config, nodes.toMutableList()).apply { start() }

    private val server = ServerBuilder.forPort(config.port)
            .addService(ClusterNodeService(controller))
            .build().apply { start() }

    fun getState(): LogState {
        return controller.state.log.state()
    }

    fun isLeader() = controller.state.current == NodeState.LEADER

    fun leaderNode() = config.hosts[controller.state.leaderId] ?: throw RuntimeException("Leader not found")

    suspend fun applyCommand(command: Command): Boolean {
        return controller.applyCommand(command)
    }

    fun await() {
        server.awaitTermination()
    }
}

