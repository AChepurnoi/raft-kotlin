package edu.ucu.raft.log

import edu.ucu.proto.LogEntry
import kotlin.collections.ArrayList
import kotlin.math.min



data class LogState(val data: Map<String, ByteArray> = emptyMap()) {
    operator fun get(key: String): ByteArray? = data[key]
}

class Log {

    @Volatile
    private var entries: MutableList<LogEntry> = ArrayList()

    var commitIndex: Int = -1
        private set

    fun lastIndex() = entries.size - 1

    fun lastTerm(): Long? = entries.lastOrNull()?.term

    fun isNotEmpty(): Boolean = entries.isNotEmpty()


    fun state(): LogState {
        return entries.take(commitIndex + 1)
                .fold(LogState()) { state: LogState, entry: LogEntry ->
                    Command.fromBytes(entry.commandBytes.toByteArray()).update(state)
                }
    }

    fun append(value: LogEntry): Int {
        val targetIndex = entries.size
        entries.add(value)
        return targetIndex
    }

    fun commit(index: Int): Boolean {
        val idx = min(lastIndex(), index)
        commitIndex = idx
        return true
    }

    fun starting(index: Int): List<LogEntry> {
        return entries.filterIndexed { i, _ -> i >= index }
    }

    operator fun get(prevLogIndex: Int) = entries.getOrNull(prevLogIndex)

    operator fun set(idx: Int, value: LogEntry) {
        if (idx == entries.size) {
            entries.add(value)
        } else {
            entries[idx] = value
        }
    }

    fun prune(startIndex: Int) {

        val drop = entries.size - startIndex
        entries = entries.dropLast(drop).toMutableList()

    }

    override fun toString(): String {
        val commited = (0..commitIndex).map { "■" }.joinToString(separator = "")
        val uncommited = ((commitIndex + 1)..lastIndex()).map { "□" }.joinToString(separator = "")
        return "[${commited + uncommited}](${commitIndex + 1}/${entries.size})"
    }


}