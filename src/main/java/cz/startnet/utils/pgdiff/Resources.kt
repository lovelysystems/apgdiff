/**
 * Copyright 2010 StartNet s.r.o.
 */
package cz.startnet.utils.pgdiff

import java.util.*

/**
 * Utility class for accessing localized resources.
 *
 * @author fordfrog
 */
object Resources {
    /**
     * Resource bundle.
     */
    private val RESOURCE_BUNDLE = ResourceBundle.getBundle("cz/startnet/utils/pgdiff/Resources")

    /**
     * Returns string from resource bundle based on the key.
     *
     * @param key key
     *
     * @return string
     */
    fun getString(key: String?): String {
        return RESOURCE_BUNDLE.getString(key)
    }
}