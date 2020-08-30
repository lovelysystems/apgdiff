package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils
import java.util.*

class PgFunction(val name: String, val schema: String) {

    /**
     * List of arguments.
     */
    val arguments: MutableList<Argument> = ArrayList()

    /**
     * Whole definition of the function from RETURNS keyword.
     */
    var body: String? = null

    /**
     * Comment.
     */
    var comment: String? = null

    var owner: String? = null

    /**
     * Returns creation SQL of the function.
     *
     * @return creation SQL
     */
    val creationSQL: String
        get() {
            val sbSQL = StringBuilder(500)
            sbSQL.append("CREATE OR REPLACE FUNCTION ")
            sbSQL.append(PgDiffUtils.getQuotedName(name))
            sbSQL.append('(')
            var addComma = false
            for (argument in arguments) {
                if (addComma) {
                    sbSQL.append(", ")
                }
                sbSQL.append(argument.getDeclaration(true))
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
            val args = arguments.map { it.getDeclaration(false) }.joinToString(", ")
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
    fun addArgument(argument: Argument) {
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
                sbString.append(argument.dataType!!.toLowerCase(Locale.ENGLISH))
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
     * @param object                   object to be compared
     * @param ignoreFunctionWhitespace whether multiple whitespaces in function
     * [.body] should be ignored
     *
     * @return true if `object` is pg function and the function code is
     * the same when compared ignoring whitespace, otherwise returns
     * false
     */
    fun equals(
        `object`: Any,
        ignoreFunctionWhitespace: Boolean
    ): Boolean {
        var equals = false
        if (this === `object`) {
            equals = true
        } else if (`object` is PgFunction) {
            val function = `object`
            if (name != function.name) {
                return false
            }
            val thisBody: String?
            val thatBody: String?
            if (ignoreFunctionWhitespace) {
                thisBody = body!!.replace("\\s+".toRegex(), " ")
                thatBody = function.body!!.replace("\\s+".toRegex(), " ")
            } else {
                thisBody = body
                thatBody = function.body
            }
            if (thisBody == null && thatBody != null
                || thisBody != null && thisBody != thatBody
            ) {
                return false
            }
            if (arguments.size != function.arguments.size) {
                return false
            } else {
                for (i in arguments.indices) {
                    if (arguments[i] != function.arguments[i]) {
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
            sbString.append(argument.getDeclaration(true))
        }
        return sbString.toString().hashCode()
    }

    /**
     * Function argument information.
     */
    class Argument {
        /**
         * Argument mode.
         */
        var mode: String? = "IN"
            set(value) {
                field = if (value.isNullOrEmpty()) "IN" else value
            }

        /**
         * Argument name.
         */
        var name: String? = null

        /**
         * Argument data type.
         */
        var dataType: String? = null

        /**
         * Argument default expression.
         */
        var defaultExpression: String? = null

        /**
         * Creates argument declaration.
         *
         * @param includeDefaultValue whether to include default value
         *
         * @return argument declaration
         */
        fun getDeclaration(includeDefaultValue: Boolean): String {
            val sbString = StringBuilder(50)
            if (mode != null && !"IN".equals(mode, ignoreCase = true)) {
                sbString.append(mode)
                sbString.append(' ')
            }
            if (!name.isNullOrEmpty()) {
                sbString.append(PgDiffUtils.getQuotedName(name))
                sbString.append(' ')
            }
            sbString.append(dataType)
            if (includeDefaultValue && !defaultExpression.isNullOrEmpty()) {
                sbString.append(" = ")
                sbString.append(defaultExpression)
            }
            return sbString.toString()
        }

        override fun equals(other: Any?): Boolean {
            if (other !is Argument) {
                return false
            } else if (this === other) {
                return true
            }
            val argument = other
            return ((if (dataType == null) argument.dataType == null else dataType.equals(
                argument.dataType,
                ignoreCase = true
            ))
                    && (if (defaultExpression == null) argument.defaultExpression == null else defaultExpression == defaultExpression)
                    && (if (mode == null) argument.mode == null else mode.equals(
                argument.mode,
                ignoreCase = true
            ))
                    && if (name == null) argument.name == null else name == argument.name)
        }

        override fun hashCode(): Int {
            val sbString = StringBuilder(50)
            sbString.append(
                if (mode == null) null else mode!!.toUpperCase(Locale.ENGLISH)
            )
            sbString.append('|')
            sbString.append(name)
            sbString.append('|')
            sbString.append(if (dataType == null) null else dataType!!.toUpperCase(Locale.ENGLISH))
            sbString.append('|')
            sbString.append(defaultExpression)
            return sbString.toString().hashCode()
        }
    }
}