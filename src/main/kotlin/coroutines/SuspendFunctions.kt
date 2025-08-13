package coroutines

import kotlinx.coroutines.suspendCancellableCoroutine
import org.slf4j.LoggerFactory

/**
 * Suspend functions
 * - Functions that can run on a coroutine
 *
 * Features and restrictions
 * - Compile to CPS (continuation passing style) with an additional Continuation argument
 * - Can only be called from other suspend functions
 * - Offer cooperative scheduling at yielding / suspension points, e.g. delays
 *
 */

object SuspendFunctions {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    suspend fun takeTheBus() { // this code can run on a coroutine
        LOGGER.info("Getting in the bus")
        (0 .. 10).forEach {
            LOGGER.info("${it * 10}% done")
            Thread.sleep(300) // yielding point - coroutine that runs this code can be suspended
            // cooperative scheduling
        }
        LOGGER.info("Getting off the bus, I'm done!")
    }

    // suspend functions cannot be run from regular functions

    // continuation = state of the code at the point a coroutine is suspended
    suspend fun demoSuspendedCoroutine() {
        LOGGER.info("Starting to run some code")

        val resumedComputation = suspendCancellableCoroutine { continuation ->
            LOGGER.info("This runs when I'm suspended")
            continuation.resumeWith(Result.success(42))
        } // yielding point - coroutine is suspended

        LOGGER.info("This prints after resuming the coroutine: $resumedComputation")

    }

    // CPS - continuation passing style (similar to fibers ZIO/CE)
    // suspend functions compile to functions with Continuations as their last argument

    // suspend function values (lambdas)
    val suspendLambda: suspend (Int) -> Int = { it + 1}
    // Key Point: (Int) -> Int and 'suspend' (Int) -> Int are different types

    val increment: suspend Int.() -> Int = { this + 1 }
    suspend fun suspendLambdaDemo() {
        LOGGER.info("Suspend call: ${suspendLambda(2)}")
        val four = 3.increment()
        println("Suspend lambda with receivers: $four")
    }

    /** ------ Why this style of main method does not work with a suspend keyword
     * the below will compile to public static void main(string[], Continuation) - what Kotlin compiles to
     * public static void main(String[]) - for the JVM
     */
//    @JvmStatic
//    suspend fun main(args: Array<String>) {
//        takeTheBus()
//    }


}

suspend fun main() {
    SuspendFunctions.takeTheBus()
}