package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils
import java.io.PrintWriter

open class DBObject(val objectType: String, val name: String) {

    var comment: String? = null
    var owner: String? = null

    open fun quotedIdentifier() = PgDiffUtils.getQuotedName(name)

    val ownerSQL: String
        get() = "ALTER $objectType ${quotedIdentifier()} OWNER TO $owner;"

    val commentSQL: String
        get() = "COMMENT ON $objectType ${quotedIdentifier()} IS $comment;"

    val dropSQL: String
        get() = "DROP $objectType IF EXISTS ${quotedIdentifier()};"

    fun commentSQL(writer: PrintWriter) {
        writer.println(commentSQL)
    }

    fun ownerSQL(writer: PrintWriter) {
        writer.println(ownerSQL)
    }

    open fun dropSQL(writer: PrintWriter) {
        writer.println(dropSQL)
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
