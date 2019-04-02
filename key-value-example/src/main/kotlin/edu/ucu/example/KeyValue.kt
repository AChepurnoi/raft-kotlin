package edu.ucu.example

import edu.ucu.raft.RaftNode
import edu.ucu.raft.log.Set
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.io.core.String

object KeyValue {

    val node = RaftNode()
    val serverPortInc = 4000

    @JvmStatic
    fun main(args: Array<String>) {
        embeddedServer(Netty, serverPortInc + node.config.port) {
            routing {
                get("/{key}") {
                    val key = call.parameters["key"]!!
                    if (node.isLeader()) {
                        val data = node.getState()[key]?.let { String(it) } ?: ""
                        call.respondText(data, ContentType.Text.Plain)
                    } else {
                        val leader = node.leaderNode()
                        call.response.header("Location", "http://${leader.host}:${leader.port + serverPortInc}/$key")
                        call.respond(HttpStatusCode.TemporaryRedirect, "redirected to master")
                    }
                }
                post("/{key}") {
                    val key = call.parameters["key"]!!
                    if (node.isLeader()) {
                        val data = call.receiveText()
                        val result = node.applyCommand(Set(key, data.toByteArray()))
                        call.respondText("Result: $result")
                    } else {
                        val leader = node.leaderNode()
                        call.response.header("Location", "http://${leader.host}:${leader.port + serverPortInc}/$key")
                        call.respond(HttpStatusCode.TemporaryRedirect, "redirected to master")
                    }

                }
            }
        }.start(true)
        node.await()
    }
}