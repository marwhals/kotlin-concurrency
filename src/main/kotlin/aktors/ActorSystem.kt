package aktors

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.coroutineScope

object ActorSystem: ActorScope() {
    suspend fun <T> app(name: String, action: suspend (ActorRef<T>) -> Unit): Unit =
        coroutineScope {
            val guardian = createActor<T>(name, this, CoroutineName(name))
            action(guardian)
        }
}