package no.rmy.wiki2epub

class Paragraph(val content: String, val isPoem: Boolean) : Tag {
    override fun html(): String = content.trim().lines().joinToString("\n").let {
        it.trim()
            .replace("</span>x<br/>", "</span>")
            .replace("</span>x", "</span>").let {
                if (isPoem || it.startsWith("<p")) {
                    it

                } else {
                    "<p>\n${
                        it
                    }\n</p>"

                }
            }
    }


    override fun epub2html(): String = html()
    override fun epub3html(): String = html()
    override fun words(): List<String> = content.trim().split("\\s+".toRegex())
    override fun wordsWithContext(): Map<String, List<String>> = content.trim().lines().flatMap { line ->
        line.replace("<[^>]*>".toRegex(), "").trim().split("\\s+".toRegex()).map {
            it to line
        }
    }.groupBy {
        it.first
    }.entries.map {
        it.key to it.value.map { it.second }
    }.toMap()


    companion object {
        fun isPageNumber(s: String): Boolean = s.startsWith("{{page|")

        fun toTag(p: List<String>, isPoem: Boolean, pageOffset: Int): Tag = // Paragraph(p.joinToString("\n"))
            if (p.size == 1 && isPageNumber(p.first())) {
                PageNumber(p.first(), pageOffset)
            } else {
                p.filter { it.isNotBlank() }.mapIndexed { index, it ->
                    if (isPageNumber(it)) {
                        PageNumber(it.trim(), pageOffset).html() // + "¤"
                    } else {
                        if (isPoem) {
                            when (index) {
                                0 -> "<div class=\"one\">$it</div>"
                                else -> "<div class=\"follow\">$it</div>"
                            }
                            //"<div class=\"line\">$it</div>"
                            //"<span class=\"line\">$it</span>"
                            //it
                        } else {
                            it.split(Regex("\\s+")).chunked(10).map {
                                it.joinToString(" ")
                            }.joinToString("\n")
                        }
                    }
                }.let {
                    Paragraph(
                        it.joinToString(
                            //"<br/>\n"
                            "\n"
                        ) { it.trim() }, isPoem
                    )
                }
            }

        fun create(content: String, isPoem: Boolean = true, pageOffset: Int): List<Tag> {
            val queue = mutableListOf<Tag>()

            val lines = content.replace("&", "&amp;").trim().lines().toMutableList()
            val p = mutableListOf<String>()
            while (lines.isNotEmpty()) {
                val line = lines.first()
                lines.removeFirst()
                var tag: Tag? = if (line.startsWith("{{midtstilt|{{stor")) {
                    Heading(line, 1)
                } else if (line.startsWith("{{midtstilt|")) {
                    Heading(line, 2)
                } else {
                    var revisedLine = line
                    while(revisedLine.contains("''")) {
                        revisedLine = revisedLine.replaceFirst("''", "<i>")
                        revisedLine = revisedLine.replaceFirst("''", "</i>")
                    }
                    // println("Line: $revisedLine")
                    listOf(
                        "{{innfelt initial ppoem|",
                        "{{page|",
                        "{{Sperret|",
                        "{{nodent|{{innfelt initial|",
                        "{{Blank linje",
                        "{{rettelse|",
                        "{{høyre|''"

                    ).forEach { searchFor ->
                        var tries = 5
                        while (--tries > 0 && revisedLine.contains(searchFor)) {
                            revisedLine.split(searchFor, limit = 2).last().split("}}").first().let { c ->
                                val oldValue = "$searchFor$c}}"
                                // println(oldValue)
                                when (searchFor) {
                                    "{{page|" -> {
                                        if (!isPoem)
                                            revisedLine = revisedLine.replace(oldValue, PageNumber(oldValue, pageOffset).html())
                                    }

                                    "{{Sperret|" -> {
                                        revisedLine = revisedLine.replace(oldValue, "<em>$c</em>")
                                    }

                                    "{{Blank linje" -> {
                                        revisedLine = revisedLine.replace(oldValue, "<br/>")
                                    }

                                    "{{innfelt initial ppoem|" -> {
                                        revisedLine = revisedLine.replace(oldValue, "<strong class=\"big\">$c</strong>")
                                    }

                                    "{{nodent|{{innfelt initial|" -> {
                                        revisedLine = revisedLine.replace(oldValue, "<strong class=\"big\">$c</strong>")
                                        revisedLine = revisedLine.replace("}}", "")
                                    }

                                    "{{høyre|''" -> {
                                        revisedLine = revisedLine.replace(oldValue, "$c")
                                        revisedLine = revisedLine.split("''").first().let {
                                            "<p class=\"right\">$it</p>"
                                        }
                                    }

                                    "{{rettelse|" -> {
                                        revisedLine = revisedLine.replace(oldValue, c.split("|").last())
                                    }

                                    else -> {
                                        revisedLine = revisedLine.replace("$searchFor$c}}", c)
                                    }
                                }
                            }
                        }
                    }
                    p.add(revisedLine)
                    null
                }
                if (tag != null) {
                    if (p.isNotEmpty()) {
                        queue.add(toTag(p, isPoem, pageOffset))
                        p.clear()
                    }
                    queue.add(tag)
                }
            }
            if (p.isNotEmpty()) {
                queue.add(toTag(p, isPoem, pageOffset))
            }
            return queue
        }
    }
}

