package edu.ucu.raft.log

import com.google.protobuf.ByteString
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.*


sealed class Command: Serializable{
    abstract fun update(logState: LogState): LogState

    fun toBytes(): ByteString{
        val baos = ByteArrayOutputStream()
        ObjectOutputStream(baos).use { it.writeObject(this) }
        val b64 = Base64.getEncoder().encodeToString(baos.toByteArray())
        return ByteString.copyFromUtf8(b64)
    }


    companion object {
        fun fromBytes(byteArray: ByteArray): Command{
            val bytes = Base64.getDecoder().decode(String(byteArray))
            val readObject = ObjectInputStream(ByteArrayInputStream(bytes)).readObject()
            return readObject as Command
        }
    }
}

class Set(private val key: String, private val value: ByteArray) : Command() {
    override fun update(logState: LogState): LogState = logState.copy(data = logState.data.toMutableMap()
            .apply {
                put(key, value)
            })
}


data class LogState(val data: Map<String, ByteArray> = emptyMap()) {
    operator fun get(key: String): ByteArray? = data[key]
}

