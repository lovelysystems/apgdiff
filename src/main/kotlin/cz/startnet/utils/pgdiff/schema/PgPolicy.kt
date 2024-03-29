/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.schema

/**
 *
 * Stores POLICY information.
 *
 */
class PgPolicy(val name: String) {
    var tableName: String? = null
    var command: String? = null
    val roles: MutableList<String?> = ArrayList()
    var using: String? = null
    var withCheck: String? = null
    fun addRole(role: String?) {
        roles.add(role)
    }
}