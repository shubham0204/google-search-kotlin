package io.shubham0204.google_search

import java.util.Collections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Google search provider
 *
 * @constructor Create empty Google search provider
 */
class GoogleSearchProvider {

    /**
     * Stores the scraped results from the Google searhc
     *
     * @constructor Create empty Google search result with all properties initialized to empty
     *   strings
     * @property title The title of the Google search result
     * @property href The URL pointing to the website of the Google search result
     * @property pageText The text of the website of the Google search result (returned only if
     *   `returnPageText` is set to `true` in `search` and `searchAsFlow`
     */
    data class GoogleSearchResult(
        var title: String = "",
        var href: String = "",
        var pageText: String = ""
    )

    /**
     * Enumeration for search timeframes,
     * provided as an argument to `search` or `searchAsFlow`
     */
    enum class SearchTimeframe(val code: String) {
        PAST_HOUR( "h" ) ,
        PAST_24HOURS( "d" ) ,
        PAST_WEEK( "w" ) ,
        PAST_MONTH( "m" ) ,
        PAST_YEAR( "y" ) ,
    }


    companion object {

        /**
         * Search Google and return the title, URL and (optional) page-text of the results
         *
         * @param term query given to the search (query param: `q`)
         * @param numResults max. number of results to return (query param: `num`)
         * @param lang language to perform search in (query param: `hl`)
         * @param safe enables SafeSearch in Google
         * @param timeoutMillis
         * @param readPageText whether to parse the text of web-pages present in search results
         * @param userAgent a user agent for querying Google Search
         * @return A list of `GoogleSearchResult` objects
         */
        suspend fun search(
            term: String,
            numResults: Int = 10,
            lang: String = "en",
            safe: String = "active",
            timeframe: SearchTimeframe? = null,
            timeoutMillis: Int = 10000,
            readPageText: Boolean = true,
            userAgent: String =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.54 Safari/537.36"
        ): List<GoogleSearchResult> =
            withContext(Dispatchers.IO) {
                val results = Collections.synchronizedList(mutableListOf<GoogleSearchResult>())
                val response = sendRequest(term, numResults, safe, lang, timeoutMillis, userAgent, timeframe)
                val divBlocks = response.select("div.g")
                runBlocking(Dispatchers.Default) {
                    divBlocks
                        .map { divBlock ->
                            launch(Dispatchers.Default) {
                                val title = divBlock.select("h3").firstOrNull()?.text() ?: ""
                                val href = divBlock.select("a").firstOrNull()?.attr("href") ?: ""
                                var pageText = ""
                                if (readPageText && href.isNotEmpty()) {
                                    val doc: Document =
                                        Jsoup.connect(href).userAgent(userAgent).get()
                                    pageText = doc.text()
                                }
                                if (title.isNotEmpty() && href.isNotEmpty()) {
                                    results.add(GoogleSearchResult(title, href, pageText))
                                }
                            }
                        }
                        .joinAll()
                }
                return@withContext results
            }


        /**
         * Search Google and return the title, URL and (optional) page-text of the results as a Kotlin
         * Flow
         *
         * @param term query given to the search (query param: `q`)
         * @param numResults max. number of results to return (query param: `num`)
         * @param lang language to perform search in (query param: `hl`)
         * @param safe enables SafeSearch in Google
         * @param timeoutMillis
         * @param readPageText whether to parse the text of web-pages present in search results
         * @param userAgent a user agent for querying Google Search
         * @return A Kotlin flow emitting `GoogleSearchResult` objects
         */
        suspend fun searchAsFlow(
            term: String,
            numResults: Int = 10,
            lang: String = "en",
            safe: String = "active",
            timeframe: SearchTimeframe? = null,
            timeoutMillis: Int = 10000,
            readPageText: Boolean = true,
            userAgent: String =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.54 Safari/537.36"
        ): Flow<GoogleSearchResult> =
            channelFlow {
                    val response =
                        sendRequest(term, numResults, safe, lang, timeoutMillis, userAgent, timeframe)
                    val divBlocks = response.select("div.g")
                    runBlocking(Dispatchers.Default) {
                        divBlocks
                            .map { divBlock ->
                                launch(Dispatchers.Default) {
                                    try {
                                        val title =
                                            divBlock.select("h3").firstOrNull()?.text() ?: ""
                                        val href =
                                            divBlock.select("a").firstOrNull()?.attr("href") ?: ""
                                        var pageText = ""
                                        if (readPageText && href.isNotEmpty()) {
                                            val doc: Document =
                                                Jsoup.connect(href).userAgent(userAgent).get()
                                            pageText = doc.text()
                                        }
                                        if (title.isNotEmpty() && href.isNotEmpty()) {
                                            send(GoogleSearchResult(title, href, pageText))
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                            .joinAll()
                    }
                }
                .flowOn(Dispatchers.IO)


        // Makes an HTTP request with the given user-agent
        // and other parameters
        private fun sendRequest(
            term: String,
            numResults: Int,
            safe: String,
            lang: String,
            timeout: Int,
            userAgent: String,
            timeframe: SearchTimeframe?
        ): Document {
            val query = term.trim().replace(" ", "+")
            var searchURL =
                "https://www.google.com/search?q=${query}&hl=$lang&safe=$safe&num=${(numResults + 2)}"
            timeframe?.let {
               searchURL += "&tbs=qdr:${it.code}"
            }
            return Jsoup.connect(searchURL).userAgent(userAgent).timeout(timeout).get()
        }
    }
}
