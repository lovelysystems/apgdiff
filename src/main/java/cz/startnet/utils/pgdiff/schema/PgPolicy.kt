/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.schema

import java.util.*

/**
 *
 * Stores POLICY information.
 *
 */
class PgPolicy {
    var name: String? = null
    var tableName: String? = null
    var command: String? = null
    val roles: MutableList<String?> = ArrayList()
    var using: String? = null
    var withCheck: String? = null
    fun addRole(role: String?) {
        roles.add(role)
    }
}