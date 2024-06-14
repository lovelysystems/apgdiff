package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils

/**
 * Function argument information.
 */
data class PgFunctionArgument(
    val mode: String,
    val name: String?,
    val dataType: String,
    val defaultExpression: String?
) {

    /**
     * Creates argument declaration.
     */
    fun getDeclaration(): String {
        val sbString = StringBuilder(50)
        if (!"IN".equals(mode, ignoreCase = true)) {
            sbString.append(mode)
            sbString.append(' ')
        }
        name?.takeIf { it.isNotEmpty() }?.let {
            sbString.append(PgDiffUtils.getQuotedName(it))
            sbString.append(' ')
        }

        sbString.append(dataType)
        if (!defaultExpression.isNullOrEmpty()) {
            sbString.append(" DEFAULT ")
            sbString.append(defaultExpression)
        }
        return sbString.toString()
    }
}