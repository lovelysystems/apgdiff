/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.schema

/**
 * Stores inherited column information.
 *
 * @author dwatson78
 */
class PgInheritedColumn(
    /**
     * Inherited column
     */
    val inheritedColumn: PgColumn
) {
    /**
     * Getter for [.inheritedColumn].
     *
     * @return [.inheritedColumn]
     */
    /**
     * Getter for [.defaultValue].
     *
     * @return [.defaultValue]
     */
    /**
     * Setter for [.defaultValue].
     *
     * @param defaultValue [.defaultValue]
     */
    /**
     * Default value of the column.
     */
    var defaultValue: String? = null
    /**
     * Getter for [.nullValue].
     *
     * @return [.nullValue]
     */
    /**
     * Setter for [.nullValue].
     *
     * @param nullValue [.nullValue]
     */
    /**
     * Determines whether null value is allowed in the column.
     */
    var nullValue = true
}