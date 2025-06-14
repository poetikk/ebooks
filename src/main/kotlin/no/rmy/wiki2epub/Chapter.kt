package no.rmy.wiki2epub

import java.io.InputStream
import kotlin.collections.joinToString


class Chapter(val content: String, val useStyle: Boolean, val pageOffset: Int, val chStart: String?) {
    val title: String
        get() = tags().mapNotNull { it as? Heading }.joinToString(" - ") {
            it.text
        }.ifBlank {
            null
        } ?: content.replace("\\{\\{.*?\\}\\}".toRegex(), "")
            .replace("}}", "")
            .replace("{{", "")
            .replace("<[^>]*>".toRegex(), "")
            .lines().filter {
            it.isNotBlank()
        }.joinToString(" ").trim().split("\\s+".toRegex()).map {
            it.trim()
        }.take(10).plus("...").let {
            listOfNotNull(
                chStart,
                it.joinToString(" "),
            ).joinToString(": ")

        }

    fun inputStream(): InputStream = html().byteInputStream()

    fun tags(): List<Tag> = if (useStyle)
        tagsPoem()
    else
        tagsNormal()

    fun calcWordUsage(): WordUsage = tags().map {
        it.wordsWithContext()
    }.let { maps ->
        val m = WordUsage()
        maps.map {
            WordUsage(it, title.split("-").first().trim())
        }.let {
            m.append(it)
        }
        m
    }

    fun tagsPoem(): List<Tag> =
        content.split(Regex("\\{\\{gap\\|1em\\}\\}|\\{\\{Innrykk\\|1\\}\\}")).map {
            Paragraph.create(it, pageOffset = pageOffset)
        }.flatten()

    fun tagsNormal(): List<Tag> =
        content.split("\n\n").map {
            it.replace("\n", " ")
        }.map {
            Paragraph.create(it, false, pageOffset)
        }.flatten()

    fun epub3(body: String): String = """
<?xml version="1.0" encoding="utf-8"?>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="nb-NO">
<head>
    <meta charset="UTF-8" />
	<title>$title</title>
    ${getStyle(useStyle)}
</head>
<body xmlns:epub="http://www.idpf.org/2007/ops" epub:type="bodymatter">
<section epub:type="chapter" class="chapter">
$body
</section>
</body>
</html>
""".trim()

    fun epub2(body: String): String = """
<?xml version="1.0" encoding="utf-8" standalone="yes"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="nb-NO" dir="ltr"
lang="nb-NO">
<head>
  <title>$title</title>
  ${getStyle(useStyle)}
</head>
<body>
${body}
</body>
</html>
""".trim()

    fun html(): String = when (Mode.current) {
        Mode.EPUB2 -> epub2html()
        Mode.EPUB3 -> epub3html()
    }


    fun epub2html(): String = tags().map {
        it.epub2html()
    }.joinToString("\n\n").let {
        epub2(it)
    }


    fun epub3html(): String = tags().map {
        it.epub3html()
    }.joinToString("\n\n").let {
        epub3(it)
    }


    companion object {
        fun getStyle(s: Boolean): String = if (s)
            "<link rel=\"stylesheet\" href=\"styles.css\" />"
        else
            "<link rel=\"stylesheet\" href=\"innledning.css\" />"

        val style = """
  <style>
  </style>

        """.trimIndent()
    }

}

