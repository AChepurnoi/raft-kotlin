package edu.ucu

import edu.ucu.log.Log
import edu.ucu.secondary.Cluster
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import kotlin.concurrent.fixedRateTimer

class Consensus(val clock: TermClock, val cluster: Cluster) {

    private val logger = KotlinLogging.logger {}

    val log = Log()
    val state = State()
    private val subscription = clock.channel.openSubscription()

    private val termIncrementListener = GlobalScope.launch {
        for (term in subscription) {
            state.nextTerm(term)
            val result = cluster.askVotes(state.id, state.term)
            if (result) {
                state.promoteToLeader()
            } else {
                logger.info { "---> 🤬 Can't promote to leader <---" }
            }
        }
    }

    private val heartbeat = fixedRateTimer("heartbeat", initialDelay = 1000, period = 1000) {
        try {
            if (state.current == NodeState.LEADER) {
                runBlocking {
                    cluster.sendLeaderHeartbeat(state.id, state.term)
                }
            }
        } catch (e: Exception) {
            logger.error { "🤬Exception happened" }
        }

    }


    private val clockStateListener = GlobalScope.launch {
        for ((prev, current) in state.updates.openSubscription()) {
            when {
                current == NodeState.LEADER -> clock.freeze()
                prev == NodeState.LEADER && current != NodeState.LEADER -> clock.start()
            }
        }
    }
    private val clog = fixedRateTimer("logger", initialDelay = 1000, period = 1000) {
        logger.info { "⛳️ $state" }
    }

    private suspend fun actualizeTerm(receivedTerm: Long){
        if(clock.term < receivedTerm){
            clock.update(receivedTerm)
        }
    }

    suspend fun grantVoteTo(candidateId: Int, term: Long): Boolean {
        actualizeTerm(term)
        val vote = state.tryVote(candidateId, term)
        logger.info { "🗽Vote requested by: ${candidateId}. Voted: $vote" }
        return vote

    }

    suspend fun validateLeaderMessage(leaderId: Int, term: Long): Boolean {
        actualizeTerm(term)
        val result = state.appendLeaderHeartbeat(leaderId, term)
        if (result) {
            clock.reset()
        }
        logger.info { "💎 Validated leader message. Result $result" }
        return result

    }




}