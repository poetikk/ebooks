package no.rmy.wiki2epub

import no.rmy.mediawiki.getAutoNamedLogger
import java.io.File

object Odysseen {
    val project = "odysseen"

    suspend fun generate() {
        val book = Book(project, "https://api.wikimedia.org/core/v1/wikisource/no/page/Side%3AOdysseen_1922.pdf%2F")
        val chapters = listOf(
            // Chapter.create(1, 6, false),
            book.createChapter(9, 20),
            book.createChapter(21, 32),
            book.createChapter(33, 45),
            book.createChapter(46, 68),
            book.createChapter(69, 82),
            book.createChapter(83, 91),
            book.createChapter(92, 101),
            book.createChapter(102, 117),
            book.createChapter(118, 132),
            book.createChapter(133, 148),
            book.createChapter(149, 166),
            book.createChapter(167, 178),
            book.createChapter(179, 190),
            book.createChapter(191, 205),
            book.createChapter(206, 220),
            book.createChapter(221, 233),
            book.createChapter(234, 250),
            book.createChapter(251, 262),
            book.createChapter(263, 279),
            book.createChapter(280, 290),
            book.createChapter(291, 302),
            book.createChapter(303, 316),
            book.createChapter(317, 327),
            book.createChapter(328, 342),
        )

        logger.info("Generating chapters complete")

        Mode.entries.forEach { currentMode ->
            Mode.current = currentMode
            when (currentMode) {
                Mode.EPUB2 -> Epub2Maker.create(project, chapters)
                Mode.EPUB3 -> Epub3Maker.create(project, chapters)
            }
        }

        logger.info("Unique Words: ${WordUsage.usages.size}")

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

    val logger = getAutoNamedLogger()
}