/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.schema.PgDatabase
import java.util.*

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
    fun getObjectName(name: String?): String {
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
    fun getSecondObjectName(name: String?): String {
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
    fun getThirdObjectName(name: String?): String? {
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
        name: String?,
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
    fun generateName(
        prefix: String?,
        names: List<String?>, postfix: String?
    ): String {
        val adjName: String?
        adjName = if (names.size == 1) {
            names[0]
        } else {
            val sbString = StringBuilder(names.size * 15)
            for (name in names) {
                if (sbString.length > 0) {
                    sbString.append(',')
                }
                sbString.append(name)
            }
            Integer.toHexString(sbString.toString().hashCode())
        }
        val sbResult = StringBuilder(30)
        if (prefix != null && !prefix.isEmpty()) {
            sbResult.append(prefix)
        }
        sbResult.append(adjName)
        if (postfix != null && !postfix.isEmpty()) {
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
    private fun splitNames(string: String?): Array<String> {
        return if (string!!.indexOf('"') == -1) {
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