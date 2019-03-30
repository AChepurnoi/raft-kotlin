package edu.ucu

import edu.ucu.log.Command
import edu.ucu.proto.AppendRequest
import edu.ucu.proto.AppendResponse
import edu.ucu.proto.VoteRequest
import edu.ucu.proto.VoteResponse
import edu.ucu.secondary.ClusterNode
import edu.ucu.secondary.GrpcClusterNode
import edu.ucu.secondary.RaftHandler
import kotlinx.coroutines.*
import mu.KotlinLogging
import kotlin.concurrent.fixedRateTimer


class RaftController(val cluster: List<GrpcClusterNode>) : RaftHandler {

    val clock = TermClock(5000)

    private val logger = KotlinLogging.logger {}

    val state = State()

    val heartbeatCommand = Heartbeat(state, cluster)
    val voting = Voting(state, cluster)
    val commit = CommitUpdate(state, cluster)


    private val subscription = clock.channel.openSubscription()


    fun start() {
        runBlocking { clock.start() }
    }

    //    ##########################
    //    #######  Listeners #######
    //    ##########################
    private val termIncrementListener = GlobalScope.launch {
        for (term in subscription) {
            onTermPassed(term)
        }
    }

    private val heartbeat = fixedRateTimer("heartbeat", initialDelay = 1000, period = 1000) {
        runBlocking {
            heartbeatCommand.send()
            commit.perform()
        }
    }

    private val clockStateListener = GlobalScope.launch {
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
    private val clog = fixedRateTimer("logger", initialDelay = 1000, period = 1000) {
        logger.info { "⛳️ $state" }
        if (state.current == NodeState.LEADER) {
            cluster.forEach {
                logger.info { "⚙️ Node: ${it.host}:${it.port} - Next: ${it.nextIndex} Match: ${it.matchIndex}" }

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

    suspend fun applyCommand(command: Command):Boolean {
        return state.applyCommand(command)
    }

}