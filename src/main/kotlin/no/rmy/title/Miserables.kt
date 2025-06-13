package no.rmy.title

import no.rmy.mediawiki.getAutoNamedLogger
import no.rmy.wiki2epub.Book
import no.rmy.wiki2epub.Epub2Maker
import no.rmy.wiki2epub.Epub3Maker
import no.rmy.wiki2epub.Mode
import no.rmy.wiki2epub.WordUsage
import java.io.File
import kotlin.collections.flatten


object Miserables {
    val project = "miserables"

    data class BookInfo(
        val url: String,
        val pageOffset: Int,
        val chapters: List<Int>,
    )

    suspend fun generate() {
        val book = Book(project, "")

        val chapters = listOf(
            BookInfo(
                "https://api.wikimedia.org/core/v1/wikisource/no/page/Side%3ADe_elendige_1.pdf%2F",
                2,
                listOf(9, 43, 103, 238, 143, 187, 200, 275, 297, 329, 342, 399 - 2)
            ),
            BookInfo(
                url = "https://api.wikimedia.org/core/v1/wikisource/no/page/Side%3ADe_elendige_2.pdf%2F",
                pageOffset = 4,
                listOf(
                    5, 20, 52, 115, 123, 136, 170, 189, 197, 213, 295, 302, 315, 336, 346, 364, 400, 419 - 4
                )
            ),
            BookInfo(
                "https://api.wikimedia.org/core/v1/wikisource/no/page/Side%3ADe_elendige_3.pdf%2F",
                4,
                listOf(
                    5, 21, 28, 41, 49, 72, 80, 99, 119, 185, 227, 241, 275, 301, 332, 347, 392 - 4
                )
            )
        ).map {
            val offset = it.pageOffset
            val url = it.url

            val firstPages = it.chapters.dropLast(1).map { page -> page + offset }
            val lastPages = it.chapters.drop(1).map { page -> page + offset - 1 }

            val chapters = firstPages.mapIndexed { index, page ->
                book.createChapter(page, lastPages.get(index), url = url)
            }
            chapters
        }.flatten()


        logger.info("Generating chapters complete")
        Mode.entries.forEach { currentMode ->
            Mode.Companion.current = currentMode
            when (currentMode) {
                Mode.EPUB2 -> Epub2Maker.create(project, chapters)
                Mode.EPUB3 -> Epub3Maker.create(project, chapters)
            }
        }

        logger.info("Unique Words: ${WordUsage.Companion.usages.size}")

        File("docs/${project}_dict.html").outputStream().writer().use { writer ->
            writer.append("<html>\n<body>\n<ul>\n")
            WordUsage.Companion.usages.toSortedMap().filter {
                it.value.size == 1
            }.forEach {
                writer.appendLine("<li>${it.key}: ${it.value}</li>")
            }
            writer.append("</ul>\n</body>\n</html>\n")
        }
    }

    val logger = getAutoNamedLogger()
}
