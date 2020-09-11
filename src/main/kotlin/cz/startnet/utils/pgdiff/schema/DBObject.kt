package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils
import java.io.PrintWriter

open class DBObject(val objectType: String, val name: String) {
    var comment: String? = null
    var owner: String? = null

    open fun quotedIdentifier() = PgDiffUtils.getQuotedName(name)

    fun commentSQL(writer: PrintWriter) {
        writer.println(
            "COMMENT ON $objectType ${quotedIdentifier()} IS $comment;"
        )
    }

    fun ownerSQL(writer: PrintWriter) {
        writer.println(
            "ALTER $objectType ${quotedIdentifier()} OWNER TO $owner;"
        )
    }

    fun dropSQL(writer: PrintWriter) {
        writer.println(
            "DROP $objectType  ${PgDiffUtils.dropIfExists} ${quotedIdentifier()};"
        )
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
        return get(name) == null
    }

    fun containsSame(other: T): Boolean {
        return contains(other.name)
    }
}
