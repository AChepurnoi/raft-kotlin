package edu.ucu.raft

import edu.ucu.raft.log.Command
import edu.ucu.proto.AppendRequest
import edu.ucu.proto.AppendResponse
import edu.ucu.proto.VoteRequest
import edu.ucu.proto.VoteResponse
import edu.ucu.raft.actions.CommitUpdate
import edu.ucu.raft.actions.Heartbeat
import edu.ucu.raft.actions.Voting
import edu.ucu.raft.adapters.ClusterNode
import edu.ucu.raft.adapters.RaftHandler
import edu.ucu.raft.clock.TermClock
import edu.ucu.raft.config.RaftConfiguration
import edu.ucu.raft.state.*
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.util.*
import kotlin.concurrent.fixedRateTimer


class RaftController(val config: RaftConfiguration,
                     val cluster: MutableList<ClusterNode> = mutableListOf()) : RaftHandler {

    val clock = TermClock(config.timerInterval)

    private val logger = KotlinLogging.logger {}

    val state = State(id = config.id)

    val heartbeatCommand = Heartbeat(state, cluster)
    val voting = Voting(state, cluster)
    val commit = CommitUpdate(state, cluster)


    private val clockSubscription = clock.channel.openSubscription()

    private lateinit var termIncrementListener: Job
    private lateinit var heartbeat: Timer
    private lateinit var clockStateListener: Job

    fun start() {
        runBlocking {
            clock.start()

            termIncrementListener = GlobalScope.launch {
                for (term in clockSubscription) {
                    onTermPassed(term)
                }
            }

            heartbeat = fixedRateTimer("heartbeat", initialDelay = config.heartbeatInterval, period = config.heartbeatInterval) {
                runBlocking {
                    heartbeatCommand.send()
                    commit.perform()
                }
            }

            clockStateListener = GlobalScope.launch {
                for ((prev, current) in state.updates.openSubscription()) {
                    when {
                        current == NodeState.LEADER -> {
                            cluster.forEach { it.reinitializeIndex(state.log.lastIndex() + 1) }
                            clock.freeze()
                        }
                        prev == NodeState.LEADER && current != NodeState.LEADER -> {
                            clock.start()
                        }
                    }
                }
            }
        }
    }

    fun stop() {
        runBlocking {
            clock.freeze()
            termIncrementListener.cancelAndJoin()
            heartbeat.cancel()
            clockStateListener.cancelAndJoin()
        }
    }

    private val clog = fixedRateTimer("logger", initialDelay = 1000, period = 1000) {
        logger.info { "⛳️ $state" }
        if (state.current == NodeState.LEADER) {
            cluster.forEach {
                logger.info { "⚙️ Node: ${it.nodeId} - Next: ${it.nextIndex} Match: ${it.matchIndex}" }
            }
        }
    }

    //    ########################
    //    ####### Handlers #######
    //    ########################
    suspend fun onTermPassed(term: Long) {
        state.nextTerm(term)
        val result = voting.askVotes()
        if (result) {
            state.promoteToLeader()
        } else {
            logger.info { "---> 🤬 Can't promote to leader <---" }
        }
    }

    //    #############################
    //    ####### GRPC Handlers #######
    //    #############################

    override suspend fun requestVote(request: VoteRequest): VoteResponse {
        actualizeTerm(request.term)
        val vote = state.requestVote(request)
        if (vote.voteGranted) {
            clock.reset()
        }
        logger.info { "🗽Vote request: ${request.candidateId} - term  ${request.term}" }
        return vote
    }

    override suspend fun appendEntries(request: AppendRequest): AppendResponse {
        actualizeTerm(request.term)
        val result = state.appendEntries(request)
        if (result.success) {
            clock.reset()
        }
        logger.info { "💎 Validated leader message. Result ${result.success}" }
        return result
    }


    private suspend fun actualizeTerm(receivedTerm: Long) {
        if (clock.term < receivedTerm) {
            clock.update(receivedTerm)
        }
    }

    suspend fun applyCommand(command: Command): Boolean {
        return if (state.current == NodeState.LEADER) return state.applyCommand(command) else false
    }

}