/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.schema

import java.util.*

/**
 * Utilities for [PgColumn].
 *
 * @author fordfrog
 */
object PgColumnUtils {
    /**
     * Returns default value for given column type. If no default value is
     * specified then null is returned.
     *
     * @param type column type
     *
     * @return found default value or null
     */
    fun getDefaultValue(type: String?): String? {
        val defaultValue: String?
        val adjType = type!!.lowercase(Locale.ENGLISH)
        defaultValue =
            if ("smallint" == adjType || "integer" == adjType || "bigint" == adjType || adjType.startsWith("decimal")
                || adjType.startsWith("numeric")
                || "real" == adjType || "double precision" == adjType || "int2" == adjType || "int4" == adjType || "int8" == adjType || adjType.startsWith(
                    "float"
                )
                || "double" == adjType || "money" == adjType
            ) {
                "0"
            } else if (adjType.startsWith("character varying")
                || adjType.startsWith("varchar")
                || adjType.startsWith("character")
                || adjType.startsWith("char")
                || "text" == adjType
            ) {
                "''"
            } else if ("boolean" == adjType) {
                "false"
            } else {
                null
            }
        return defaultValue
    }
}