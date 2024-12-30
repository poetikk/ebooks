package no.rmy.wiki2epub

open class WordUsage {
    val usages: MutableMap<String, MutableList<String>>

    constructor(u: Map<String, List<String>>) {
        usages = u.entries.associate {
            it.key to it.value.toMutableList()
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
