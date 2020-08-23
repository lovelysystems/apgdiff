/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.schema

import java.util.*

/**
 * Stores database information.
 *
 * @author fordfrog
 */
class PgDatabase {
    /**
     * List of database schemas.
     */
    val schemas: MutableList<PgSchema> = ArrayList(1)

    /**
     * Array of ignored statements.
     */
    val ignoredStatements: MutableList<String> = ArrayList()
    /**
     * Getter for [.defaultSchema].
     *
     * @return [.defaultSchema]
     */
    /**
     * Current default schema.
     */
    lateinit var defaultSchema: PgSchema
        private set
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
     * List of extensions.
     */
    val extensions: MutableList<PgExtension> = ArrayList()

    /**
     * Sets default schema according to the `name` of the schema.
     *
     * @param name name of the schema
     */
    fun setDefaultSchema(name: String) {
        defaultSchema = getSchema(name)!!
    }

//    /**
//     * Getter for [.ignoredStatements].
//     *
//     * @return [.ignoredStatements]
//     */
//    fun getIgnoredStatements(): List<String> {
//        return Collections.unmodifiableList(ignoredStatements)
//    }

    /**
     * Adds ignored statement to the list of ignored statements.
     *
     * @param ignoredStatement ignored statement
     */
    fun addIgnoredStatement(ignoredStatement: String) {
        ignoredStatements.add(ignoredStatement)
    }

    /**
     * Returns schema of given name or null if the schema has not been found. If
     * schema name is null then default schema is returned.
     *
     * @param name schema name or null which means default schema
     *
     * @return found schema or null
     */
    fun getSchema(name: String?): PgSchema? {
        if (name == null) {
            return defaultSchema
        }
        for (schema in schemas) {
            if (schema.name == name) {
                return schema
            }
        }
        return null
    }

//    /**
//     * Getter for [.schemas]. The list cannot be modified.
//     *
//     * @return [.schemas]
//     */
//    fun getSchemas(): List<PgSchema?> {
//        return Collections.unmodifiableList(schemas)
//    }

    /**
     * Adds `schema` to the lists of schemas.
     *
     * @param schema schema
     */
    fun addSchema(schema: PgSchema) {
        schemas.add(schema)
    }

//    /**
//     * Getter for [.extensions]. The list cannot be modified.
//     *
//     * @return [.extensions]
//     */
//    fun getExtensions(): List<PgExtension> {
//        return Collections.unmodifiableList(extensions)
//    }

    /**
     * Adds `extension` to the list of extensions.
     *
     * @param extension extension
     */
    fun addExtension(extension: PgExtension) {
        extensions.add(extension)
    }

    /**
     * Returns extension of given name or null if the extension has not been found.
     *
     * @param name extension name
     * @return found extension or null
     */
    fun getExtension(name: String?): PgExtension? {
        for (extension in extensions) {
            if (extension.name == name) {
                return extension
            }
        }
        return null
    }

    /**
     * Creates a new PgDatabase object.
     */
    init {
        schemas.add(PgSchema("public"))
        defaultSchema = schemas[0]
    }
}