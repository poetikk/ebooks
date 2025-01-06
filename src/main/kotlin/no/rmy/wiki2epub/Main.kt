package no.rmy.wiki2epub

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import no.rmy.mediawiki.getAutoNamedLogger
import java.io.File



fun main(): Unit = runBlocking {
    Odysseen.generate()
    // Iliaden.generate()
}


class Page(val page: Int, val source: String?) {
    val content
        get() = cleanedContent().lines().filter {
            it.trim().let {
                !it.startsWith("{{ppoem")
                        && !it.equals("}}")
            }
        }.joinToString("\n")

    constructor(page: Int, json: JsonElement) : this(
        page,
        json.jsonObject.get("source")?.jsonPrimitive?.contentOrNull
    )

    private val oldPageString: String = "<span epub:type=\"pagebreak\" id=\"page$page\">$page</span>"
    private val pageString: String get() = "{{page|$page}}"

    override fun toString(): String = "$pageString\n$content"


    private fun cleanedContent(): String {
        return source?.let { text ->
            text.split("</noinclude>").filter {
                !it.startsWith("<noinclude>")
            }.map {
                it.split("<noinclude>").first()
            }.joinToString("")
        } ?: ""
    }
}


interface Tag {
    fun words(): List<String>
    fun wordsWithContext(): Map<String, List<String>>
    fun html(): String
    fun epub2html(): String
    fun epub3html(): String
}

