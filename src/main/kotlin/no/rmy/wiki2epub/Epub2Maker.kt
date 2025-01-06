package no.rmy.wiki2epub

import io.documentnode.epub4j.domain.Author
import io.documentnode.epub4j.domain.Book
import io.documentnode.epub4j.domain.Resource
import io.documentnode.epub4j.epub.EpubWriter
import java.io.File
import java.io.FileOutputStream
import java.util.*


object Epub2Maker {
    fun create(project: String, chapters: List<Chapter>) {
        val title = project.replaceFirstChar { it.titlecase(Locale.getDefault()) }
        Mode.current = Mode.EPUB2

        val path = when (Mode.current) {
            Mode.EPUB2 -> "files/$project/epub2"
            Mode.EPUB3 -> "files/$project/epub3"
        }
        File(path).mkdirs()

        chapters.forEachIndexed { index, ch ->
            val filename = "$path/chapter_$index.xhtml"
            File(filename).writeText(ch.html())
        }

        val ebook = Book().apply {
            metadata.apply {
                titles.add(title)
                contributors.add(Author("Homer"))
                contributors.add(Author("Peder", "Østbye (oversetter)"))
                contributors.add(Author("Øystein", "Tvede (digital utgave)"))
                publishers.add("H. ASCHEHOUG & CO. (W. NYGAARD)")
            }

            Resource(File("${project}_cover.jpg").inputStream(), "${project}_cover.jpg").let {
                setCoverImage(it)
            }
            Resource(File("styles.css").inputStream(), "styles.css").let {
                addResource(it)
            }
            Resource(File("innledning.css").inputStream(), "innledning.css").let {
                addResource(it)
            }


            when (Mode.current) {
                Mode.EPUB2 -> "kolofon_$project.xhtml"
                Mode.EPUB3 -> "kolofon3_$project.xhtml"
            }.let { filename ->
                Resource(File(filename).inputStream(), "tittelside.xhtml").let {
                    addResource(it)
                    spine.addResource(it)
                }
            }
            chapters.forEachIndexed { index, ch ->
                val chIndex = index + 1
                val chapterResource = Resource(ch.inputStream(), "chapter_$chIndex.xhtml")

                this.addSection(ch.title, chapterResource)
            }

        }


        val ebookWriter = EpubWriter()
        when (Mode.current) {
            Mode.EPUB2 -> "${project}_epub2.epub"
            Mode.EPUB3 -> "${project}.epub"
        }.let {
            ebookWriter.write(ebook, FileOutputStream("docs/download/$it"))
        }

    }
}
