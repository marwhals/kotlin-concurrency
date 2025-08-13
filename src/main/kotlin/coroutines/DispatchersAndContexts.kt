package coroutines

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.random.Random

/**
 * Dispatchers
 * - Thread pool + scheduler of coroutines
 * Types of dispatchers
 * - Default: regular thread pool, for most scenarios
 * ---- Can configure thread pool size by JVM configs
 * ---- Can alter parallelism (with an experimental API)
 * - IO: for blocking tasks, e.g database connections or UI
 * - Main: for the app's main thread
 * - Unconfined - for launching cheap coroutines on the calling thread where it is not important where they resume
 * ----> ^^^^ should generally be avoided
 *
 * Coroutine Contexts
 * - Data structures with info that can be surfaced in suspend functions
 * Info that can be added to coroutine contexts
 * - dispatcher
 * - job handle
 * - coroutine id and name
 * - thread-local data (for interacting with Java code using ThreadLocal)
 * - your own values
 */

object DispatchersAndContexts {
    val LOGGER = LoggerFactory.getLogger(this::class.java)

    // dispatcher = thread pool + scheduler of coroutines

    val basicDispatcher: CoroutineDispatcher = Dispatchers.Default

    suspend fun thing() {
        val basicDispatcher: CoroutineDispatcher = Dispatchers.Default
        val limitedDispatcher = basicDispatcher.limitedParallelism(1)
        coroutineScope {
            launch(limitedDispatcher) { developer() }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    suspend fun demoDispatchers() {
        val limitedDispatcher = basicDispatcher.limitedParallelism(1) // single-threaded dispatcher
        val singleThreadedDispatcher = newSingleThreadContext("TheOneThread")

        LOGGER.info("Demo limited dispatcher")
        coroutineScope {
            launch(limitedDispatcher) {
                for (i in (1..100)) {
                    LOGGER.info("Running task $i")
                }
            }
            launch(limitedDispatcher) {
                LOGGER.info("The first tasks must end...")
            }
            launch(limitedDispatcher) {
                for (i in (200..300)) {
                    LOGGER.info("Running task $i")
                }
            }
            launch(limitedDispatcher) {
                LOGGER.info("... before the last one does.")
            }
        } // coroutines on single threaded dispatcher/single thread will run sequentially

        LOGGER.info("Demo unconfined dispatcher")
        coroutineScope {
            launch(Dispatchers.Unconfined) {
                LOGGER.info("Unconfined - start") // will run on the calling thread
                delay(500) // suspension point
                LOGGER.info("Unconfined - resumed")
            }

            launch {
                LOGGER.info("Regular - start")
                delay(500) // suspension point
                LOGGER.info("Regular - resumed")
            }
        }
    }

    /*
        Dispatcher types
        - Default - thread pool between 2 and N_CORES (number of cores) ---> follow through to documentation
            - can configure kotlinx.coroutines.scheduler.core.pool.size and kotlinx.coroutines.scheduler.max.pool.size as JVM args
            -> use this for regular coroutines
        - IO - used for blocking actions (UI tasks / waiting for a socket / database tasks)
            - more complex design
            - thread pool max(N_CORES, 64)
        - Main - for the main app this is usually single-threaded
        - Unconfined - not bound by a certain thread pool
            - runs a coroutine in the calling thread,
            - suspends at the first suspension point
            - resumes on the thread that caused that suspension
            - good for starting cheap coroutines, when you don't care where they're resumed
              ---> use sparingly
     */

    // contexts
    val aContext: CoroutineContext = basicDispatcher
    val coroutineName: CoroutineContext = CoroutineName("myCoroutine")
    val combinedContext = aContext + coroutineName
    val nameExtracted = combinedContext[CoroutineName] // CoroutineName("myCoroutine")

    suspend fun developer() {
        // coroutineContext is available from all suspend functions
        val coroutineName = coroutineContext[CoroutineName]?.name ?: "unknown"
        LOGGER.info("I'm a developer: $coroutineName. I need to write code or I'll die. No really.")
        delay(Random.nextLong(1000))
        LOGGER.info("I wrote code today")
    }

    suspend fun startup() {
        LOGGER.info("9AM, let's start")
        coroutineScope {
            launch(context = CoroutineName("Alice")) { developer() }
            launch(context = CoroutineName("Bob")) { developer() }
        }
        LOGGER.info("6PM, we'll never make it")
    }

    // contexts are inherited to child coroutines
    suspend fun startupInheritance() {
        LOGGER.info("9AM, let's start")
        coroutineScope {
            launch(context = CoroutineName("Team A")) {
                // child coroutines will inherit the Team A name
                launch { developer() }
                launch { developer() }

                // may be overridden
                launch(context = CoroutineName("Team lead")) { developer() }

                // can override for multiple children
                withContext(CoroutineName("All stars")) {
                    launch { developer() }
                    launch { developer() }
                }
            }
        }
        LOGGER.info("6PM, we'll never make it")
    }

    /* What kind of information can be put in a coroutines context......hmmmmmmmmmmmmmm
        - dispatcher
        - coroutine id
        - coroutine name
        - job handle
        - your own fields
        - thread local
     */

    class TeamName(val name: String): CoroutineContext.Element {
        override val key: CoroutineContext.Key<*> = Key
        companion object Key: CoroutineContext.Key<TeamName>
    }

    suspend fun developerWithTeam() {
        // coroutineContext is available from all suspend functions
        val name = coroutineContext[CoroutineName]?.name ?: "unknown"
        val teamName = coroutineContext[TeamName]?.name ?: "unknown"
        LOGGER.info("I'm a developer: $name working for $teamName. I need to write code or I'll die.")
        delay(Random.nextLong(1000))
        LOGGER.info("I wrote code today")
    }

    suspend fun startupComplexContext() {
        LOGGER.info("9AM, let's start")
        coroutineScope {
            launch(context = CoroutineName("Alice") + TeamName("Analytics")) { developerWithTeam() }
            launch(context = CoroutineName("Bob") + TeamName("Frontend")) { developerWithTeam() }
        }
        LOGGER.info("6PM, we'll never make it")
    }
}

suspend fun main () {
    DispatchersAndContexts.startupComplexContext()
}