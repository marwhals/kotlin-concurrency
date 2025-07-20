package aktors

import kotlinx.coroutines.channels.*

/**
 * A small version of the Akka actors library
 * - Uses
 * -- Co routine scopes
 * -- Managing co routines and jobs
 * -- Co routine contexts
 * -- Channel
 *
 * Actors - Data strcutures that are scehduled on OS threads
 * - Are created as data structures
 * - Do something only when we send a message to them
 * - Process messages sequentially - upon reception of a message, they
 * --- Can change internal state
 * --- Can send messages to other actors
 * --- Run arbitrary code
 * --- Change their behaviour for subsequent messages
 * - Can receive multiple messages in a queue, aka a mailbox
 * - Fully process a message once started
 * - Can be de-scheduled in between processing messages
 * - Can create other (child) actors
 * - Do not expose or share internal state
 *
 * Motivation
 * - We don't need to care about thread/coroutine management
 * - We can more easily separate concerns: every actor does one thing
 * - We can easily make modules of out application "talk" to each other asynchronously
 * - We can intuitively model interactions in real life
 * - We can use the same API for distributed systems (as an extension)
 *
 *
 */

/*
- receives a message a certain type
- wraps a coroutine channel
- a method tell(msg: your type) -> push an element to that channel
- a method '!' or actor ! msg
 */


class ActorRef<T>(private val mailbox: SendChannel<T>) {
    suspend fun tell(msg: T) =
        mailbox.send(msg)

    suspend infix fun `!`(msg: T) =
        tell(msg)


}