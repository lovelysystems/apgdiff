/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.schema.PgDatabase

/**
 * Parser utilities.
 *
 * @author fordfrog
 */
object ParserUtils {
    /**
     * Returns object name from optionally schema qualified name.
     *
     * @param name optionally schema qualified name
     *
     * @return name of the object
     */
    fun getObjectName(name: String): String {
        val names = splitNames(name)
        return names[names.size - 1]
    }

    /**
     * Returns second (from right) object name from optionally schema qualified
     * name.
     *
     * @param name optionally schema qualified name
     *
     * @return name of the object
     */
    fun getSecondObjectName(name: String): String {
        val names = splitNames(name)
        return names[names.size - 2]
    }

    /**
     * Returns third (from right) object name from optionally schema qualified
     * name.
     *
     * @param name optionally schema qualified name
     *
     * @return name of the object or null if there is no third object name
     */
    fun getThirdObjectName(name: String): String? {
        val names = splitNames(name)
        return if (names.size >= 3) names[names.size - 3] else null
    }

    /**
     * Returns schema name from optionally schema qualified name.
     *
     * @param name     optionally schema qualified name
     * @param database database
     *
     * @return name of the schema
     */
    fun getSchemaName(
        name: String,
        database: PgDatabase
    ): String {
        val names = splitNames(name)
        return if (names.size < 2) {
            database.defaultSchema.name
        } else {
            names[0]
        }
    }

    /**
     * Generates unique name from the prefix, list of names, and postfix.
     *
     * @param prefix  prefix
     * @param names   list of names
     * @param postfix postfix
     *
     * @return generated name
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun generateName(
        prefix: String?,
        names: List<String?>, postfix: String?
    ): String {
        val adjName: String? = if (names.size == 1) {
            names[0]
        } else {
            val sbString = StringBuilder(names.size * 15)
            for (name in names) {
                if (sbString.isNotEmpty()) {
                    sbString.append(',')
                }
                sbString.append(name)
            }
            sbString.toString().hashCode().toHexString()
        }
        val sbResult = StringBuilder(30)
        if (!prefix.isNullOrEmpty()) {
            sbResult.append(prefix)
        }
        sbResult.append(adjName)
        if (!postfix.isNullOrEmpty()) {
            sbResult.append(postfix)
        }
        return sbResult.toString()
    }

    /**
     * Splits qualified names by dots. If names are quoted then quotes are
     * removed.
     *
     * @param string qualified name
     *
     * @return array of names
     */
    fun splitNames(string: String): Array<String> {
        return if (string.indexOf('"') == -1) {
            string.split(".").toTypedArray()
        } else {
            val strings: MutableList<String> = ArrayList(2)
            var startPos = 0
            while (true) {
                startPos = if (string[startPos] == '"') {
                    val endPos = string.indexOf('"', startPos + 1)
                    strings.add(string.substring(startPos + 1, endPos))
                    if (endPos + 1 == string.length) {
                        break
                    } else if (string[endPos + 1] == '.') {
                        endPos + 2
                    } else {
                        endPos + 1
                    }
                } else {
                    val endPos = string.indexOf('.', startPos)
                    if (endPos == -1) {
                        strings.add(string.substring(startPos))
                        break
                    } else {
                        strings.add(string.substring(startPos, endPos))
                        endPos + 1
                    }
                }
            }
            strings.toTypedArray()
        }
    }
}
