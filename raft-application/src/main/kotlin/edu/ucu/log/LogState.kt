package edu.ucu.log


sealed class Command {
    abstract fun update(logState: LogState): LogState
}

class Set(private val key: String, private val value: String) : Command() {
    override fun update(logState: LogState): LogState = logState.copy(data = logState.data.toMutableMap()
            .apply {
                put(key, value)
            })
}


data class LogState(val data: Map<String, String> = emptyMap()) {
    operator fun get(key: String): String? = data[key]
}

