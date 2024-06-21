package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils
import kotlin.text.StringBuilder

data class DomainConstraint(val name: String, val check: String) {

    fun quotedIdentifier() = PgDiffUtils.getQuotedName(name)

    override fun toString() = sql()

    fun sql(): String {
        return "CONSTRAINT ${quotedIdentifier()} CHECK ${check}"
    }
}


class PgDomain(name: String, position: Int) : DBObject("DOMAIN", name, position) {

    var default: String? = null
    var dataType: String? = null

    var collation: String? = null
    var constraints = mutableListOf<DomainConstraint>()
    var notNull: Boolean = false

    fun alterSQL(writer: StringBuilder, suffix: String) {
        writer.appendLine("ALTER DOMAIN ${quotedIdentifier()} $suffix")
    }

    fun creationSQL(writer: StringBuilder) {
        // todo schema handling
        writer.append("CREATE DOMAIN ${quotedIdentifier()} AS $dataType")
        collation?.let {
            writer.append(" COLLATE $collation")
        }
        default?.let {
            writer.append(" DEFAULT $default")
        }
        if (notNull) {
            writer.append(" NOT NULL")
        }
        constraints.forEach {
            writer.append("\n    ${it.sql()}")
        }
        writer.appendLine(";")
        owner?.let {
            ownerSQL(writer)
        }
    }
}

