package aktors

import kotlinx.coroutines.channels.Channel
import org.slf4j.LoggerFactory

internal class Actor<T>(private val name: String, private val channel: Channel<T>) {

    private val log = LoggerFactory.getLogger(this::class.java)

    suspend fun run() {
        while(true) {
            val msg = channel.receive() // semantically blocking
            log.info("[$name] $msg")
        }
    }


}