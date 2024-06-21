package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils
import kotlin.text.StringBuilder

class PgExtension(val name: String) {

    lateinit var schema: PgSchema
    var version: String? = null

    var comment: String? = null

    /**
     * Previous version of the extension.
     */
    var from: String? = null

    /**
     * Returns creation SQL of the function.
     *
     * @return creation SQL
     */
    val creationSQL: String
        get() {
            val sbSQL = StringBuilder()
            sbSQL.append("CREATE EXTENSION ")
            sbSQL.append(PgDiffUtils.createIfNotExists)
            sbSQL.append(PgDiffUtils.getQuotedName(name))
            sbSQL.append(" SCHEMA ")
            sbSQL.append(schema.name)
            if (version != null && version!!.isNotEmpty()) {
                sbSQL.append(" VERSION ")
                sbSQL.append(version)
            }
            if (from != null && from!!.isNotEmpty()) {
                sbSQL.append(" FROM ")
                sbSQL.append(from)
            }
            sbSQL.append(';')
            return sbSQL.toString()
        }

    fun commentSQL(writer: StringBuilder) {
        writer.appendLine(
            "COMMENT ON EXTENSION ${PgDiffUtils.getQuotedName(name)} IS $comment;"
        )
    }

    override fun equals(other: Any?): Boolean {
        var equals = false
        if (this === other) {
            equals = true
        } else if (other is PgExtension) {
            equals = name == other.name && from == other.from && version == other.version
        }
        return equals
    }

    override fun hashCode(): Int {
        return (this::class.qualifiedName + "|" + name + "|" + version + "|"
                + from).hashCode()
    }
}