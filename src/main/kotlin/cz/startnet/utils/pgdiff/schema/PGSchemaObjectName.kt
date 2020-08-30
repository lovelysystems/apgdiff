package cz.startnet.utils.pgdiff.schema

/**
 * Qualified name of a top level object in a schema
 */
data class PGSchemaObjectName(val schema: String, val name: String)