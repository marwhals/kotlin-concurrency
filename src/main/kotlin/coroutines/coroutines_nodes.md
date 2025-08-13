# Notes

---

## Coroutines

- Similar concept to JVM virtual threads
  - small data structures allocated on the heap
  - scheduling on a small number of OS threads
  - cooperative scheduling
- Benefits vs virtual threads
  - Explicit in the Kotlin language
  - Kotlin-specific APIs and mental model
  - Much better cancellation support
  - Clear structured concurrency and lifecycle management
  - Supervision

---

# Why coroutines

## Is this the same concept?

- JVM Virtual Threads
  - small data structures allocated on the heap
  - scheduling on a small number of OS threads
  - cooperative scheduling
- Coroutines
  - small data structures allocated on the heap
  - scheduling on a small number of OS threads
  - cooperative scheduling

## Coroutines equivalent to virtual threads?

- Creation
  - Virtual Threads: Thread.ofVirtual().start { ... }
  - Coroutines: launch { ... }
- Semantic Blocking
  - Virtual threads: thread.join, Thread.sleep
  - Coroutines: job.join, delay
- Cancellation
  - Virtual threads: thread.interrupt
  - Coroutines: job.cancel
- Getting values
  - Virtual threads: Future + get
  - Coroutines: async + await
- Race conditions
  - same problems

## Coroutines ---- Nicer mental models *TBD*

- Writing blocking code like normal code
  - suspend function with language restrictions
  - semantic blocking
- Structured concurrency
  - coroutine scopes
  - context and cancellation propagation to children
- Race conditions
  - No need to mutable variables
  - Combined with functional programming ---> race conditions are impossible
- Clarity
  - few functions with suspension points
  - easy to read and understand code
  - easy to manage which coroutine runs where and who waits for whom