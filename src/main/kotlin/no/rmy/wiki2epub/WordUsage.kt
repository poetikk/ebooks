package no.rmy.wiki2epub

open class WordUsage {
    val usages: MutableMap<String, MutableList<String>>

    constructor(u: Map<String, List<String>>, title: String) {
        usages = u.entries.associate {
            val key = it.key.lowercase()
                // .replace("\\p{Punct}".toRegex(), "")
                .replace("[^\\p{L}\\s]".toRegex(), "")
            key to it.value.map { "${it.replace("<[^>]*>".toRegex(), "")} ($title)" }.toMutableList()
        }.toMutableMap()
    }

    constructor() {
        usages = mutableMapOf()
    }

    fun append(wu: WordUsage) {
        wu.usages.forEach { (key, value) ->
            usages.getOrPut(key) {
                mutableListOf()
            }.addAll(value)
        }
    }

    fun append(wuList: List<WordUsage>) {
        wuList.forEach { append(it) }
    }

    companion object: WordUsage()
}
