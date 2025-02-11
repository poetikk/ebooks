package no.rmy.wiki2epub

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import no.rmy.mediawiki.getAutoNamedLogger
import java.io.File

class Book(val project: String, val url: String, val pageOffset: Int) {
    class TooManyRequestsException(url: String): Exception("Too many requests when fetching $url")

    suspend fun createChapter(firstPage: Int, lastPage: Int, style: Boolean = true): Chapter {
        val httpClient = HttpClient(CIO)
        val jsonDecoder = Json {
            isLenient = true
        }

        val c = (firstPage..lastPage).mapNotNull { page ->
            val pageUrl = "$url$page"

            val path = "files/$project"
            File(path).mkdirs()
            val filename = "$path/${project}_$page.wikimedia"
            logger.info(filename)

            if (File(filename).exists()) {
                File(filename).readText().let {
                    Page(page, it)
                }
            } else {
                val result = httpClient.request {
                    url(pageUrl)
                }
                if(result.status == HttpStatusCode.TooManyRequests) {
                    throw TooManyRequestsException(pageUrl)
                }
                delay(3500)

                val string = result.bodyAsText()
                val source =
                    jsonDecoder.parseToJsonElement(string).jsonObject.get("source")?.jsonPrimitive?.contentOrNull
                if (source != null) {
                    File(filename).writeText(source)
                    Page(page, source)
                } else {
                    null
                }
            }
        }.joinToString("\n") {
            it.toString()
        }


        return Chapter(c, style, pageOffset).also {
            WordUsage.append(it.calcWordUsage())
        }
    }

    companion object {
        val logger  = getAutoNamedLogger()
    }
}

