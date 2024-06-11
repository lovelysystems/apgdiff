package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils
import cz.startnet.utils.pgdiff.println
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
        writer.println(commentSQL)
    }

    fun ownerSQL(writer: StringBuilder) {
        writer.println(ownerSQL)
    }

    override fun toString(): String {
        return "DBObject($objectType $name)"
    }
}


class DBObjectContainer<T : DBObject> : ArrayList<T>() {

    fun get(name: String): T? {
        return firstOrNull { it.name == name }
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
}
