package edu.ucu

object Configuration {
    val env = System.getenv()

    val port = env.getOrDefault("PORT", "4040").toInt()
    val nodes: List<Pair<String, Int>> = env.getOrDefault("NODES", "localhost:4040")
            .split(",")
            .map {
                val uri = it.split(":")
                Pair(uri[0], uri[1].toInt())
            }.toList()

    val id = env.getOrDefault("ID", (Math.random() * 100).toInt().toString()).toInt()

}