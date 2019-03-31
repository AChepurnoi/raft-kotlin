package edu.ucu.raft.config

data class RaftNode(val id: Int, val host: String, val port: Int)

interface RaftConfiguration {
    val port: Int
    val nodes: List<RaftNode>
    val id: Int
    val timerInterval: Long
    val heartbeatInterval: Long
}

class StaticConfiguration(override val id: Int, override val port: Int = 4000,
                          override val timerInterval: Long = 1000,
                          override val heartbeatInterval: Long = 500) : RaftConfiguration {
    override val nodes: List<RaftNode> = mutableListOf()
}

class EnvConfiguration : RaftConfiguration {

    val env = System.getenv()
    override val port = env.getOrDefault("PORT", "4040").toInt()
    override val nodes: List<RaftNode> = env.getOrDefault("NODES", "localhost:4040")
            .split(",")
            .map {
                val uri = it.split(":")
                RaftNode(1, uri[0], uri[1].toInt())
            }.toList()

    override val id = env.getOrDefault("ID", (Math.random() * 100).toInt().toString()).toInt()

    override val timerInterval: Long = 5000

    override val heartbeatInterval: Long = 1000


}