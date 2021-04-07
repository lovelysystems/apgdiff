package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils
import java.util.*


class PgFunction(val name: String, val schema: String) {

    /**
     * List of arguments.
     */
    val arguments: MutableList<PgFunctionArgument> = ArrayList()

    /**
     * Whole definition of the function from RETURNS keyword.
     */
    var body: String? = null

    /**
     * Comment.
     */
    var comment: String? = null

    var owner: String? = null

    fun creationSQL(replace: Boolean = true): String {
        val sbSQL = StringBuilder(500)
        if (replace) {
            sbSQL.append("CREATE OR REPLACE FUNCTION ")
        } else {
            sbSQL.append("CREATE FUNCTION ")
        }
        sbSQL.append(PgDiffUtils.getQuotedName(name))
        sbSQL.append('(')
        var addComma = false
        for (argument in arguments) {
            if (addComma) {
                sbSQL.append(", ")
            }
            sbSQL.append(argument.getDeclaration())
            addComma = true
        }
        sbSQL.append(") ")
        sbSQL.append(body)
        sbSQL.append(';')
        if (!comment.isNullOrEmpty()) {
            sbSQL.append(System.getProperty("line.separator"))
            sbSQL.append(System.getProperty("line.separator"))
            sbSQL.append("COMMENT ON FUNCTION ")
            sbSQL.append(signatureSQL)
            sbSQL.append(" IS ")
            sbSQL.append(comment)
            sbSQL.append(';')
        }
        if (owner != null) {
            sbSQL.append(System.getProperty("line.separator"))
            sbSQL.append(System.getProperty("line.separator"))
            sbSQL.append("ALTER FUNCTION $signatureSQL OWNER TO $owner;")
        }
        return sbSQL.toString()
    }

    val signatureSQL: String
        get() {
            val args = arguments.joinToString(", ") { it.dataType }
            return "${PgDiffUtils.getQuotedName(name)}($args)"
        }

    /**
     * Creates and returns SQL for dropping the function.
     *
     * @return created SQL
     */
    val dropSQL: String
        get() {
            val sbString = StringBuilder(100)
            sbString.append("DROP FUNCTION ")
            sbString.append(PgDiffUtils.dropIfExists)
            sbString.append(signatureSQL)
            sbString.append(";")
            return sbString.toString()
        }

    /**
     * Adds argument to the list of arguments.
     *
     * @param argument argument
     */
    fun addArgument(argument: PgFunctionArgument) {
        arguments.add(argument)
    }

    /**
     * Returns function signature. It consists of unquoted name and argument
     * data types.
     *
     * @return function signature
     */
    val signature: String
        get() {
            val sbString = StringBuilder(100)
            sbString.append(name)
            sbString.append('(')
            var addComma = false
            for (argument in arguments) {
                if ("OUT".equals(argument.mode, ignoreCase = true)) {
                    continue
                }
                if (addComma) {
                    sbString.append(',')
                }
                sbString.append(argument.dataType.toLowerCase(Locale.ENGLISH))
                addComma = true
            }
            sbString.append(')')
            return sbString.toString()
        }

    override fun equals(other: Any?): Boolean {
        if (other !is PgFunction) {
            return false
        } else if (other === this) {
            return true
        }
        return equals(other, false)
    }

    /**
     * Compares two objects whether they are equal. If both objects are of the
     * same class but they equal just in whitespace in [.body], they are
     * considered being equal.
     *
     * @param `object`                   object to be compared
     * @param ignoreFunctionWhitespace whether multiple whitespaces in function
     * [.body] should be ignored
     *
     * @return true if `object` is pg function and the function code is
     * the same when compared ignoring whitespace, otherwise returns
     * false
     */
    fun equals(other: Any, ignoreFunctionWhitespace: Boolean): Boolean {
        var equals = false
        if (this === other) {
            equals = true
        } else if (other is PgFunction) {
            if (name != other.name) {
                return false
            }
            val thisBody: String?
            val thatBody: String?
            if (ignoreFunctionWhitespace) {
                thisBody = body!!.replace("\\s+".toRegex(), " ")
                thatBody = other.body!!.replace("\\s+".toRegex(), " ")
            } else {
                thisBody = body
                thatBody = other.body
            }
            if (thisBody == null && thatBody != null
                || thisBody != null && thisBody != thatBody
            ) {
                return false
            }
            if (arguments.size != other.arguments.size) {
                return false
            } else {
                for (i in arguments.indices) {
                    if (arguments[i] != other.arguments[i]) {
                        return false
                    }
                }
            }
            return true
        }
        return equals
    }

    override fun hashCode(): Int {
        val sbString = StringBuilder(500)
        sbString.append(body)
        sbString.append('|')
        sbString.append(name)
        for (argument in arguments) {
            sbString.append('|')
            sbString.append(argument.getDeclaration())
        }
        return sbString.toString().hashCode()
    }

}