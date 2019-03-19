package edu.ucu.log

import java.util.*


class Log(currentTerm: Long = 0,
          private val committed: Queue<Command> = LinkedList(),
          private val pending: Queue<Command> = LinkedList()) {

    var currentTerm: Long = currentTerm
        private set



    var logState: LogState
        private set(value) = throw RuntimeException("Can't set")
        get() = committed.fold(LogState(data = emptyMap())) { cState, command -> command.update(cState) }


    fun nextTerm(): Log = this.apply { currentTerm += 1 }

    fun append(c: Command): Log = this.apply { pending.add(c) }

    fun commit(logState: LogState): LogState {

        var resultState = logState
        for (command in pending) {
            resultState = command.update(resultState)
            committed.add(command)
        }
        pending.clear()
        return resultState
    }


}