package coroutines

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import java.net.URI
import kotlin.random.Random

/**
 * Goal
 * - Coroutine scopes contain (=limit) the lifecycle of a coroutine
 * ---- always wait for all coroutines
 * ---- can start other coroutine scopes
 * - Coroutine scopes are a means of controlling
 * ---- which coroutines run concurrently
 * ---- which coroutines/groups of coroutines must wait for others to finish
 * --> Structured concurrency
 */

/**
 * Coroutine Scopes
 * - Useful because they
 * --- Allow creating arbitary coroutines within them
 * --- Can write blocking code withouth actually starving OS threads
 * --- Can ensure all coroutines finish before continuing
 * --- Can handle safe cancellation in case something fails inside
 * - Difficult withouth coroutines because
 * --- JVM threads are resource-heavy (solved by virtual threads) --- mental model remains the same
 * --- Need some kind of async way of handling results: callback hells etc
 * --- Can easily lead to synchornisation issues
 */

object StructuredConcurrency {
    val LOGGER = LoggerFactory.getLogger(this::class.java)

    suspend fun fetchHTML(url: String): String {
        LOGGER.info("Fetching page for $url")
        delay(1000)
        return URI(url).toURL().readText()
    }

    suspend fun processData(data: String): String {
        delay(500)
        return "Processed: ${data.split("\n")[0]}"
    }

    suspend fun fetchAndProcess(): String =
        coroutineScope {
            val urls = listOf(
                "https://url1.com",
                "https://url2.com",
                "https://url3.com"
            )

            // group of coroutines 1
            val deferredResults = urls.map { url ->
                async { fetchHTML(url) }
            }

            // wait for all
            val results = deferredResults.awaitAll()

            // group of coroutines 2
            val deferredData = results.map { data ->
                async { processData(data) }
            }

            // wait for all
            deferredData.awaitAll().joinToString(separator = "\n")
        }

    // nested coroutine scopes
    suspend fun fetchAndProcessDataNested() =
        coroutineScope {
            val urls = listOf(
                "https://abcde.com",
                "https://12345.com",
                "https://someUrl.com"
            )

            // group of coroutines 1
            val deferredResults = urls.map { url ->
                async { fetchHTML(url) }
            }

            // wait for all
            val results = deferredResults.awaitAll()

            // group of coroutines 2
            val deferredData = results.map { data ->
                async { processData(data) }
            }

            // wait for all
            deferredData.awaitAll().joinToString(separator = "\n")

        }

    suspend fun demoCoroutineGroups() {
        LOGGER.info("Starting data fetch...")
        val result = fetchAndProcessDataNested()
        LOGGER.info("Final result:\n$result")

        /**
         * Exercise - web crawler
         *
         * 1. Implement the `scrape` function which fetches all the pages for a website
         *      scrape("rockthejvm.com", ["courses/kotlin", "courses/coroutines"])
         *      - call fetchDataFromPage on ALL pages in the list in parallel
         *          fetchDataFromPage("rockthejvm.com/courses/kotlin")
         *          fetchDataFromPage("rockthejvm.com/courses/coroutines")
         *      - aggregate the results
         *          "Report for rockthejvm.com: $...."
         *
         * 2. Write a function to scrape MULTIPLE websites in parallel, then combine their data.
         *  - for every website, fetch its pages
         *  - then call `scrape` for every website with its pages
         */
    }

    suspend fun fetchDataFromPage(pageUrl: String): String {
        delay(Random.nextLong(1000)) // simulate network delay
        return "Data from $pageUrl"
    }

    suspend fun fetchPageUrlsFromSite(root: String): List<String> {
        delay(Random.nextLong(1000))
        return listOf("about", "privacy", "blog", "products")
    }

    suspend fun scrape(site: String, pages: List<String>): String =
        coroutineScope {
            LOGGER.info("Scraping $site")
            val pageUrls = pages.map { "$site/$it" }
            val pageData = pageUrls.map { url ->
                async {
                    LOGGER.info("Fetching page $url")
                    fetchDataFromPage(url)
                }
            }.awaitAll()

            LOGGER.info("Scraping $site complete")
            pageData.joinToString(prefix = "Report for $site:\n", separator = "\n")
        }

    // Part 2
    suspend fun crawl(sites: List<String>): String =
        coroutineScope {
            LOGGER.info("STARTING CRAWLER")
            val siteResults: List<String> = sites.map { site ->
                async {
                    val pages = fetchPageUrlsFromSite(site)
                    scrape(site, pages)
                }
            }.awaitAll()
            LOGGER.info("CRAWLER DONE")
            siteResults.joinToString(prefix = "FINAL CRAWLER REPORT:\n", separator = "\n")
        }

    suspend fun demoScraping() {
        val rockthejvmReport = scrape("rockthejvm.com", listOf("courses/kotlin", "courses/coroutines"))
        LOGGER.info(rockthejvmReport)
    }

    suspend fun demoCrawler() {
        val crawlerReport = crawl(listOf("rockthejvm.com", "coderprodigy.com", "5tobrain.com"))
        LOGGER.info(crawlerReport)
    }
}


suspend fun main() {
    StructuredConcurrency.demoCrawler()
}