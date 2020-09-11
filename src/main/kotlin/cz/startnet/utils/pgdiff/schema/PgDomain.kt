package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils
import java.io.PrintWriter

data class DomainConstraint(val name: String, val check: String) {

    fun quotedIdentifier() = PgDiffUtils.getQuotedName(name)

    override fun toString() = sql()

    fun sql(): String {
        return "CONSTRAINT ${quotedIdentifier()} CHECK ${check}"
    }
}


class PgDomain(name: String) : DBObject("DOMAIN", name) {

    var default: String? = null
    var dataType: String? = null

    var collation: String? = null
    var constraints = mutableListOf<DomainConstraint>()
    var notNull: Boolean = false

    fun alterSQL(writer: PrintWriter, suffix: String) {
        writer.println("ALTER DOMAIN ${quotedIdentifier()} $suffix")
    }

    fun creationSQL(writer: PrintWriter) {
        // todo schema handling
        writer.print("CREATE DOMAIN ${quotedIdentifier()} AS $dataType")
        collation?.let {
            writer.print(" COLLATE $collation")
        }
        default?.let {
            writer.print(" DEFAULT $default")
        }
        if (notNull) {
            writer.print(" NOT NULL")
        }
        constraints.forEach {
            writer.print("\n    ${it.sql()}")
        }
        writer.println(";")
        owner?.let {
            ownerSQL(writer)
        }
    }
}

