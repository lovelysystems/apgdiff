package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils
import kotlin.text.StringBuilder

open class DBObject(val objectType: String, val name: String, val position: Int) {

    var comment: String? = null
    var owner: String? = null

    open fun quotedIdentifier() = PgDiffUtils.getQuotedName(name)

    val ownerSQL: String
        get() = "ALTER $objectType ${quotedIdentifier()} OWNER TO $owner;"

    open val commentSQL: String
        get() = "COMMENT ON $objectType ${quotedIdentifier()} IS $comment;"

    // use this property only if the object needs to be dropped immediately, e.g. when ALTER is not implemented
    // normal drops are done by the drop visitor
    val dropSQL: String
        get() = "DROP $objectType IF EXISTS ${quotedIdentifier()} CASCADE;"

    fun commentSQL(writer: StringBuilder) {
        writer.appendLine(commentSQL)
    }

    fun ownerSQL(writer: StringBuilder) {
        writer.appendLine(ownerSQL)
    }

    override fun toString(): String {
        return "DBObject($objectType $name)"
    }
}


class DBObjectContainer<T : DBObject> (private val objects: MutableList<T> = mutableListOf()):Iterable<T> {



    fun get(name: String): T? {
        return objects.firstOrNull { it.name == name }
    }

    fun getSame(other: T): T? {
        return get(other.name)
    }

    fun contains(name: String): Boolean {
        return get(name) != null
    }

    fun containsSame(other: T): Boolean {
        return contains(other.name)
    }

    override fun iterator(): Iterator<T> {
        return objects.iterator()
    }

    fun add(obj: T) {
        objects.add(obj)
    }
}
