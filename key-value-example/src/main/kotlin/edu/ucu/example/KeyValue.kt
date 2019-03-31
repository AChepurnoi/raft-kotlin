package edu.ucu.example

import edu.ucu.raft.RaftNode
import edu.ucu.raft.config.EnvConfiguration
import edu.ucu.raft.log.Set
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

object KeyValue {

    val node = RaftNode()

    @JvmStatic
    fun main(args: Array<String>) {
        embeddedServer(Netty, EnvConfiguration().port + 4000) {
            routing {
                get("/{key}") {
                    val key = call.parameters["key"]!!
                    val data = node.getState()[key]?.let { String(it) } ?: "Nil"
                    call.respondText(data, ContentType.Text.Plain)
                }
                post("/{key}") {
                    val key = call.parameters["key"]!!
                    val data = call.receiveText()
                    val result = node.applyCommand(Set(key, data.toByteArray()))
                    call.respondText("Result: $result")
                }
            }
        }.start(true)
        node.await()
    }
}