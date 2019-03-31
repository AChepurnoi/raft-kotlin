package edu.ucu.raft

import edu.ucu.raft.config.StaticConfiguration
import edu.ucu.raft.log.Set
import edu.ucu.raft.state.NodeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner

@RunWith(BlockJUnit4ClassRunner::class)
class RaftClusterTesting {

    private fun clusterOfThree(): List<LocalRaftNode> {
        val nodeOne = LocalRaftNode(RaftController(StaticConfiguration(1, timerInterval = 500, heartbeatInterval = 250)))
        val nodeTwo = LocalRaftNode(RaftController(StaticConfiguration(2, timerInterval = 550, heartbeatInterval = 250)))
        val nodeThree = LocalRaftNode(RaftController(StaticConfiguration(3, timerInterval = 650, heartbeatInterval = 250)))

        nodeOne.controller.cluster.addAll(mutableListOf(nodeTwo, nodeThree))
        nodeTwo.controller.cluster.addAll(mutableListOf(nodeOne, nodeThree))
        nodeThree.controller.cluster.addAll(mutableListOf(nodeTwo, nodeOne))

        return listOf(nodeOne, nodeTwo, nodeThree)
    }

    @Test(timeout = 30000)
    fun clusterIsStableAndLeaderIsElected() = test {

        val nodes = clusterOfThree()
        nodes.forEach { it.activate() }
        delay(3000)

        val followers = nodes.filter { it.controller.state.current == NodeState.FOLLOWER }.count()
        val leaders = nodes.filter { it.controller.state.current == NodeState.LEADER }.count()

        assertThat(followers).isEqualTo(2)
        assertThat(leaders).isEqualTo(1)
    }


    @Test(timeout = 30000)
    fun nodeCanBeIsolatedFromClusterAndCatchUpAfter() = test {
        val nodes = clusterOfThree()
        nodes.forEach { it.activate() }

        delay(3000)
        val leader = nodes.find { it.controller.state.current == NodeState.LEADER }!!
        val oldTerm = leader.controller.state.term

        leader.isolate()
        delay(3000)
        leader.activate()
        delay(3000)
        val newTerm = leader.controller.state.term
        assertThat(newTerm).isGreaterThan(oldTerm)
        assertThat(leader.controller.state.current).isEqualTo(NodeState.FOLLOWER)
    }

    @Test(timeout = 30000)
    fun logReplicationIsWorkingCorrectly() = test {
        val nodes = clusterOfThree()
        nodes.forEach { it.activate() }

        delay(3000)
        val leader = nodes.find { it.controller.state.current == NodeState.LEADER }!!


        leader.controller.applyCommand(Set("one", "1".toByteArray()))
        leader.controller.applyCommand(Set("two", "2".toByteArray()))
        leader.controller.applyCommand(Set("three", "3".toByteArray()))

        delay(1000)

        val logs = nodes.map { it.controller.state.log.state() }

        assertThat(logs.all { String(it["one"]!!) == "1" })
        assertThat(logs.all { String(it["two"]!!) == "2" })
        assertThat(logs.all { String(it["three"]!!) == "3" })
    }

    @Test(timeout = 30000)
    fun separatingNodeFromClusterAndPuttingBackSynchronizesLogAndLeftUncommitedStaleLeader() = test {
        val nodes = clusterOfThree()
        nodes.forEach { it.activate() }
        delay(3000)
        val staleLeader = kotlin.run {
            val leader = nodes.find { it.controller.state.current == NodeState.LEADER }!!
            leader.controller.applyCommand(Set("one", "1".toByteArray()))
            val log = leader.controller.state.log.state()
            assertThat(String(log["one"]!!)).isEqualTo("1")
            delay(1000)
            leader.isolate()
            leader
        }


        val newLeader = kotlin.run {
            val staleLeaderLog = staleLeader.controller.state.log.state()
            val newLeader = nodes.find { it.controller.state.current == NodeState.LEADER }!!
            withTimeoutOrNull(500) { newLeader.controller.applyCommand(Set("two", "2".toByteArray())) }
            withTimeoutOrNull(500) { newLeader.controller.applyCommand(Set("three", "3".toByteArray())) }
            val log = newLeader.controller.state.log
            assertThat(log.lastIndex()).isEqualTo(2)
            assertThat(log.commitIndex).isEqualTo(0)
            delay(1000)
            assertThat(staleLeaderLog["two"]).isNull()
            assertThat(staleLeaderLog["three"]).isNull()
            newLeader
        }
        staleLeader.activate()
        delay(3000)

        val staleLeaderLog = staleLeader.controller.state.log.state()
        assertThat(String(staleLeaderLog["one"]!!)).isEqualTo("1")
        assertThat(staleLeaderLog["two"]).isNull()
        assertThat(staleLeaderLog["three"]).isNull()

        val leader = nodes.find { it.controller.state.current == NodeState.LEADER }!!
        leader.controller.applyCommand(Set("two", "2".toByteArray()))
        leader.controller.applyCommand(Set("three", "3".toByteArray()))
        val leaderLog = leader.controller.state.log.state()
        assertThat(String(leaderLog["two"]!!)).isEqualTo("2")
        assertThat(String(leaderLog["three"]!!)).isEqualTo("3")

    }





}