package no.rmy.wiki2epub

import no.rmy.mediawiki.getAutoNamedLogger
import java.io.File

object Iliaden {
    val project = "iliaden"
    val url = "https://api.wikimedia.org/core/v1/wikisource/no/page/Side%3AIliaden.djvu%2F"

    suspend fun generate() {
        val book = Book(project, url)
        val chapters = listOf(
            // Chapter.create(1, 6, false),

            book.createChapter(7, 10, false),
            book.createChapter(11, 27),
            book.createChapter(28, 51),
            book.createChapter(52, 64),
            book.createChapter(65, 79),
            book.createChapter(80, 104),
            book.createChapter(105, 119),
            book.createChapter(120, 132),
            book.createChapter(133, 148),
            book.createChapter(149, 168),
            book.createChapter(169, 184),
            book.createChapter(185, 207),
            book.createChapter(208, 220),
            book.createChapter(221, 243),
            book.createChapter(244, 258),
            book.createChapter(259, 278),
            book.createChapter(279, 302),
            book.createChapter(303, 323),
            book.createChapter(324, 340),
            book.createChapter(341, 352),
            book.createChapter(353, 366),
            book.createChapter(367, 383),
            book.createChapter(384, 397),
            book.createChapter(398, 421),
            book.createChapter(422, 443),
        )

        Mode.entries.forEach { currentMode ->
            Mode.current = currentMode
            when(currentMode) {
                Mode.EPUB2 -> Epub2Maker.create(project, chapters)
                Mode.EPUB3 -> Epub3Maker.create(project, chapters)
            }
        }



        getAutoNamedLogger().let { logger ->
            logger.info("Unique Words: ${WordUsage.usages.size}")
        }

        File("docs/${project}_dict.html").outputStream().writer().use { writer ->
            writer.append("<html>\n<body>\n<ul>\n")
            WordUsage.usages.toSortedMap().filter {
                it.value.size == 1
            }.forEach {
                writer.appendLine("<li>${it.key}: ${it.value}</li>")
            }
            writer.append("</ul>\n</body>\n</html>\n")
        }
    }
}
