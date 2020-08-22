/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.schema

import cz.startnet.utils.pgdiff.PgDiffUtils
import java.util.*

/**
 * Stores function information.
 *
 * @author fordfrog
 */
class PgFunction {
    /**
     * Getter for [.name].
     *
     * @return [.name]
     */
    /**
     * Setter for [.name].
     *
     * @param name [.name]
     */
    /**
     * Name of the function including argument types.
     */
    var name: String? = null

    /**
     * List of arguments.
     */
    val arguments: MutableList<Argument> = ArrayList()
    /**
     * Getter for [.body].
     *
     * @return [.body]
     */
    /**
     * Setter for [.body].
     *
     * @param body [.body]
     */
    /**
     * Whole definition of the function from RETURNS keyword.
     */
    var body: String? = null
    /**
     * Getter for [.comment].
     *
     * @return [.comment]
     */
    /**
     * Setter for [.comment].
     *
     * @param comment [.comment]
     */
    /**
     * Comment.
     */
    var comment: String? = null

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
            if (comment != null && !comment!!.isEmpty()) {
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append(System.getProperty("line.separator"))
                sbSQL.append("COMMENT ON FUNCTION ")
                sbSQL.append(PgDiffUtils.getQuotedName(name))
                sbSQL.append('(')
                addComma = false
                for (argument in arguments) {
                    if (addComma) {
                        sbSQL.append(", ")
                    }
                    sbSQL.append(argument.getDeclaration(false))
                    addComma = true
                }
                sbSQL.append(") IS ")
                sbSQL.append(comment)
                sbSQL.append(';')
            }
            return sbSQL.toString()
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
            sbString.append(name)
            sbString.append('(')
            var addComma = false
            for (argument in arguments) {
                if ("OUT".equals(argument.mode, ignoreCase = true)) {
                    continue
                }
                if (addComma) {
                    sbString.append(", ")
                }
                sbString.append(argument.getDeclaration(false))
                addComma = true
            }
            sbString.append(");")
            return sbString.toString()
        }

//    /**
//     * Getter for [.arguments]. List cannot be modified.
//     *
//     * @return [.arguments]
//     */
//    fun getArguments(): List<Argument> {
//        return Collections.unmodifiableList(arguments)
//    }

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

    override fun equals(`object`: Any?): Boolean {
        if (`object` !is PgFunction) {
            return false
        } else if (`object` === this) {
            return true
        }
        return equals(`object`, false)
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
            if (name == null && function.name != null
                || name != null && name != function.name
            ) {
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
         * Getter for [.name].
         *
         * @return [.name]
         */
        /**
         * Setter for [.name].
         *
         * @param name [.name]
         */
        /**
         * Argument name.
         */
        var name: String? = null
        /**
         * Getter for [.dataType].
         *
         * @return [.dataType]
         */
        /**
         * Setter for [.dataType].
         *
         * @param dataType [.dataType]
         */
        /**
         * Argument data type.
         */
        var dataType: String? = null
        /**
         * Getter for [.defaultExpression].
         *
         * @return [.defaultExpression]
         */
        /**
         * Setter for [.defaultExpression].
         *
         * @param defaultExpression [.defaultExpression]
         */
        /**
         * Argument default expression.
         */
        var defaultExpression: String? = null

//        /**
//         * Getter for [.mode].
//         *
//         * @return [.mode]
//         */
//        fun getMode(): String? {
//            return mode
//        }

//        /**
//         * Setter for [.mode].
//         *
//         * @param mode [.mode]
//         */
//        fun setMode(mode: String?) {
//            this.mode = if (mode == null || mode.isEmpty()) "IN" else mode
//        }
//
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
            if (name != null && !name!!.isEmpty()) {
                sbString.append(PgDiffUtils.getQuotedName(name))
                sbString.append(' ')
            }
            sbString.append(dataType)
            if (includeDefaultValue && defaultExpression != null && !defaultExpression!!.isEmpty()) {
                sbString.append(" = ")
                sbString.append(defaultExpression)
            }
            return sbString.toString()
        }

        override fun equals(obj: Any?): Boolean {
            if (obj !is Argument) {
                return false
            } else if (this === obj) {
                return true
            }
            val argument = obj
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