package aktors

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

open class ActorScope {
    protected fun <T> createActor(
        name: String,
        scope: CoroutineScope,
        context: CoroutineContext
    ): ActorRef<T> {
        val mailbox = Channel<T>(capacity = Channel.UNLIMITED) // can be configured
        scope.launch(context) {
            val actor = Actor(name, mailbox)
            actor.run()
        }
        return ActorRef(mailbox)
    }
}