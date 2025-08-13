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