package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils
import cz.startnet.utils.pgdiff.parsers.ParserUtils

fun String.toQualifiedName(defaultSchema: String? = null): QualifiedName {
    val names = ParserUtils.splitNames(this)
    return when (names.size) {
        1 -> {
            if (defaultSchema == null) {
                error("unqualified name with no default schema cannot be resolved: $this")
            }
            QualifiedName(defaultSchema, names[0])
        }
        2 -> QualifiedName(names[0], names[1])

        else -> error("cannot resolve name with more than two parts $this")
    }
}


data class QualifiedName(val schema: String, val name: String) {

    override fun toString(): String {
        return "${PgDiffUtils.getQuotedName(schema)}.${PgDiffUtils.getQuotedName(name)}"
    }

}