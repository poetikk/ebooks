package no.rmy.wiki2epub

import net.seeseekey.epubwriter.model.EpubBook
import net.seeseekey.epubwriter.model.Landmark
import net.seeseekey.epubwriter.model.TocLink
import java.io.File
import java.util.*


object Epub3Maker {
    fun create(project: String, chapters: List<Chapter>) {
        val title = project.replaceFirstChar {
            it.titlecase(Locale.getDefault())
        }
        Mode.current = no.rmy.wiki2epub.Mode.EPUB3

        val path = when (Mode.current) {
            Mode.EPUB2 -> "files/$project/epub2"
            Mode.EPUB3 -> "files/$project/epub3"
        }
        File(path).mkdirs()

        chapters.forEachIndexed { index, ch ->
            val filename = "$path/chapter_$index.xhtml"
            File(filename).writeText(ch.html())
        }

        EpubBook(
            "nb-NO",
            UUID.randomUUID().toString(),
            title,
            "Homer"
        ).also { book ->
            book.publisher = "H. ASCHEHOUG & CO. (W. NYGAARD)"
            // Add content


            File("${project}_cover.jpg").inputStream().let {
                book.addCoverImage(
                    it.readAllBytes(),
                    "image/jpeg",
                    "images/cover.jpg"
                );
            }

            listOf("styles.css", "innledning.css").forEach { filename ->
                File(filename).inputStream().let {
                    book.addContent(
                        it,
                        "text/css", filename,
                        false,
                        false
                    )
                }
            }

            // Create Landmarks
            val landmarks: MutableList<Landmark> = mutableListOf()

            // Create toc
            val tocLinks: MutableList<TocLink> = ArrayList<TocLink>()

            listOf("omslag_$project.xhtml", "kolofon3_$project.xhtml").filter {
                File(it).exists()
            }.forEach { filename ->
                val href = filename.replace("3", "")
                val id = href.split(".", "_").first()
                //val title = id.uppercase() + "."
                File(filename).inputStream().let {
                    book.addContent(
                        it,
                        "application/xhtml+xml", href,
                        false,
                        true
                    ).setId(id)
                }
                val sectionToc: TocLink = TocLink(href, title, null)
                // tocLinks.add(sectionToc)

                val landmark = Landmark()
                when(id) {
                    "omslag" -> landmark.setType("cover")
                    "kolofon" -> landmark.setType("bodymatter")
                }
                landmark.setHref(href)
                landmark.setTitle(title)

                // landmarks.add(landmark)
            }

            chapters.forEachIndexed { index, ch ->
                val chIndex = index + 1

                val href = "chapter_$chIndex.xhtml"

                book.addContent(
                    ch.inputStream(),
                    "application/xhtml+xml", href,
                    true,
                    true
                ).setId(href.split(".").first())

                val chapterToc: TocLink = TocLink(href, ch.title, null)
                tocLinks.add(chapterToc)
            }

            // Set toc options
            book.setAutoToc(false)
            book.setTocLinks(tocLinks)

            // Set landmarks
            book.setLandmarks(landmarks)

            when (Mode.current) {
                Mode.EPUB2 -> "${project}_epub2.epub"
                Mode.EPUB3 -> "${project}.epub"
            }.let {
                book.writeToFile("docs/download/$it")
            }
        }
    }

}
