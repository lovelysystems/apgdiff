package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils

class PgExtension(val name: String) {

    lateinit var schema: PgSchema
    var version: String? = null

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
            if (schema != null) {
                sbSQL.append(" SCHEMA ")
                sbSQL.append(schema.name)
            }
            if (version != null && !version!!.isEmpty()) {
                sbSQL.append(" VERSION ")
                sbSQL.append(version)
            }
            if (from != null && !from!!.isEmpty()) {
                sbSQL.append(" FROM ")
                sbSQL.append(from)
            }
            sbSQL.append(';')
            return sbSQL.toString()
        }

    override fun equals(`object`: Any?): Boolean {
        var equals = false
        if (this === `object`) {
            equals = true
        } else if (`object` is PgExtension) {
            val extension = `object`
            equals = name == extension.name && from == extension.from && version == extension.version
        }
        return equals
    }

    override fun hashCode(): Int {
        return (javaClass.name + "|" + name + "|" + version + "|"
                + from).hashCode()
    }
}