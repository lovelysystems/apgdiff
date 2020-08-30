/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.loader.PgDumpLoader
import cz.startnet.utils.pgdiff.schema.PgDatabase
import java.io.InputStream
import java.io.PrintWriter

/**
 * Creates diff of two database schemas.
 *
 * @author fordfrog
 */
object PgDiff {
    /**
     * Creates diff on the two database schemas.
     *
     * @param writer    writer the output should be written to
     * @param arguments object containing arguments settings
     */
    fun createDiff(
        writer: PrintWriter,
        arguments: PgDiffArguments
    ) {
        // Avoid reading twice from System.in
        if (arguments.oldDumpFile == "-" && arguments.newDumpFile == "-") return
        val oldDatabase = PgDumpLoader.loadDatabaseSchema(
            arguments.oldDumpFile, arguments.inCharsetName,
            arguments.isOutputIgnoredStatements,
            arguments.isIgnoreSlonyTriggers,
            arguments.isIgnoreSchemaCreation
        )
        val newDatabase = PgDumpLoader.loadDatabaseSchema(
            arguments.newDumpFile, arguments.inCharsetName,
            arguments.isOutputIgnoredStatements,
            arguments.isIgnoreSlonyTriggers,
            arguments.isIgnoreSchemaCreation
        )
        diffDatabaseSchemas(writer, arguments, oldDatabase, newDatabase)
    }

    /**
     * Creates diff on the two database schemas.
     *
     * @param writer         writer the output should be written to
     * @param arguments      object containing arguments settings
     * @param oldInputStream input stream of file containing dump of the
     * original schema
     * @param newInputStream input stream of file containing dump of the new
     * schema
     */
    fun createDiff(
        writer: PrintWriter,
        arguments: PgDiffArguments, oldInputStream: InputStream?,
        newInputStream: InputStream?
    ) {
        val oldDatabase = PgDumpLoader.loadDatabaseSchema(
            oldInputStream, arguments.inCharsetName,
            arguments.isOutputIgnoredStatements,
            arguments.isIgnoreSlonyTriggers,
            arguments.isIgnoreSchemaCreation
        )
        val newDatabase = PgDumpLoader.loadDatabaseSchema(
            newInputStream, arguments.inCharsetName,
            arguments.isOutputIgnoredStatements,
            arguments.isIgnoreSlonyTriggers,
            arguments.isIgnoreSchemaCreation
        )
        diffDatabaseSchemas(writer, arguments, oldDatabase, newDatabase)
    }

    /**
     * Creates new schemas (not the objects inside the schemas).
     *
     * @param writer      writer the output should be written to
     * @param oldDatabase original database schema
     * @param newDatabase new database schema
     */
    private fun createNewSchemas(
        writer: PrintWriter,
        oldDatabase: PgDatabase,
        newDatabase: PgDatabase
    ) {
        for (newSchema in newDatabase.schemas) {
            val oldSchema = oldDatabase.getSchema(newSchema.name)
            if (oldSchema == null) {
                writer.println()
                writer.println(newSchema.creationSQL)
            } else if (newSchema.owner != oldSchema.owner) {
                writer.println(newSchema.ownerSQL)
            }
        }
    }

    /**
     * Creates new extensions.
     *
     * @param writer      writer the output should be written to
     * @param oldDatabase original database schema
     * @param newDatabase new database schema
     */
    private fun createNewExtensions(
        writer: PrintWriter,
        oldDatabase: PgDatabase?, newDatabase: PgDatabase?
    ) {
        for (newExtension in newDatabase!!.extensions) {
            if (oldDatabase!!.getExtension(newExtension.name) == null) {
                writer.println()
                writer.println(newExtension.creationSQL)
            }
        }
    }

    /**
     * Creates diff from comparison of two database schemas.
     *
     * @param writer      writer the output should be written to
     * @param arguments   object containing arguments settings
     * @param oldDatabase original database schema
     * @param newDatabase new database schema
     */
    private fun diffDatabaseSchemas(
        writer: PrintWriter,
        arguments: PgDiffArguments, oldDatabase: PgDatabase,
        newDatabase: PgDatabase
    ) {
        if (arguments.isAddTransaction) {
            writer.println("START TRANSACTION;")
        }
        if (oldDatabase.comment == null
            && newDatabase.comment != null
            || oldDatabase.comment != null && newDatabase.comment != null && oldDatabase.comment != newDatabase.comment
        ) {
            writer.println()
            writer.print("COMMENT ON DATABASE current_database() IS ")
            writer.print(newDatabase.comment)
            writer.println(';')
        } else if (oldDatabase.comment != null
            && newDatabase.comment == null
        ) {
            writer.println()
            writer.println("COMMENT ON DATABASE current_database() IS NULL;")
        }
        dropOldSchemas(writer, oldDatabase, newDatabase)
        createNewSchemas(writer, oldDatabase, newDatabase)
        dropOldExtensions(writer, oldDatabase, newDatabase)
        createNewExtensions(writer, oldDatabase, newDatabase)
        updateSchemas(writer, arguments, oldDatabase, newDatabase)
        if (arguments.isAddTransaction) {
            writer.println()
            writer.println("COMMIT TRANSACTION;")
        }
        if (arguments.isOutputIgnoredStatements) {
            if (!oldDatabase.ignoredStatements.isEmpty()) {
                writer.println()
                writer.print("/* ")
                writer.println(
                    Resources.getString(
                        "OriginalDatabaseIgnoredStatements"
                    )
                )
                for (statement in oldDatabase.ignoredStatements) {
                    writer.println()
                    writer.println(statement)
                }
                writer.println("*/")
            }
            if (!newDatabase.ignoredStatements.isEmpty()) {
                writer.println()
                writer.print("/* ")
                writer.println(
                    Resources.getString("NewDatabaseIgnoredStatements")
                )
                for (statement in newDatabase.ignoredStatements) {
                    writer.println()
                    writer.println(statement)
                }
                writer.println("*/")
            }
        }
    }

    /**
     * Drops old schemas that do not exist anymore.
     *
     * @param writer      writer the output should be written to
     * @param oldDatabase original database schema
     * @param newDatabase new database schema
     */
    private fun dropOldSchemas(
        writer: PrintWriter,
        oldDatabase: PgDatabase?, newDatabase: PgDatabase?
    ) {
        for (oldSchema in oldDatabase!!.schemas) {
            if (newDatabase!!.getSchema(oldSchema.name) == null) {
                writer.println()
                writer.println(
                    "DROP SCHEMA " + PgDiffUtils.dropIfExists
                            + PgDiffUtils.getQuotedName(oldSchema.name)
                            + " CASCADE;"
                )
            }
        }
    }

    /**
     * Drops old extensions that do not exist anymore.
     *
     * @param writer      writer the output should be written to
     * @param oldDatabase original database schema
     * @param newDatabase new database schema
     */
    private fun dropOldExtensions(
        writer: PrintWriter,
        oldDatabase: PgDatabase?, newDatabase: PgDatabase?
    ) {
        for (oldExtension in oldDatabase!!.extensions) {
            if (newDatabase!!.getExtension(oldExtension.name) == null) {
                writer.println()
                writer.println(
                    "DROP EXTENSION " + PgDiffUtils.dropIfExists
                            + PgDiffUtils.getQuotedName(oldExtension.name)
                            + " CASCADE;"
                )
            }
        }
    }

    /**
     * Updates objects in schemas.
     *
     * @param writer      writer the output should be written to
     * @param arguments   object containing arguments settings
     * @param oldDatabase original database schema
     * @param newDatabase new database schema
     */
    private fun updateSchemas(
        writer: PrintWriter,
        arguments: PgDiffArguments, oldDatabase: PgDatabase,
        newDatabase: PgDatabase
    ) {
        val setSearchPath = (newDatabase.schemas.size > 1
                || newDatabase.schemas[0].name != "public")
        for (newSchema in newDatabase.schemas) {
            val searchPathHelper: SearchPathHelper
            searchPathHelper = if (setSearchPath) {
                SearchPathHelper(
                    "SET search_path = "
                            + PgDiffUtils.getQuotedName(newSchema.name, true)
                            + ", pg_catalog;"
                )
            } else {
                SearchPathHelper(null)
            }
            val oldSchema = oldDatabase.getSchema(newSchema.name)
            if (oldSchema != null) {
                if (oldSchema.comment == null
                    && newSchema.comment != null
                    || oldSchema.comment != null && newSchema.comment != null && oldSchema.comment != newSchema.comment
                ) {
                    writer.println()
                    writer.print("COMMENT ON SCHEMA ")
                    writer.print(
                        PgDiffUtils.getQuotedName(newSchema.name)
                    )
                    writer.print(" IS ")
                    writer.print(newSchema.comment)
                    writer.println(';')
                } else if (oldSchema.comment != null
                    && newSchema.comment == null
                ) {
                    writer.println()
                    writer.print("COMMENT ON SCHEMA ")
                    writer.print(
                        PgDiffUtils.getQuotedName(newSchema.name)
                    )
                    writer.println(" IS NULL;")
                }
            }
            PgDiffTriggers.dropTriggers(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffRules.dropRules(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffFunctions.dropFunctions(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffViews.dropViews(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffConstraints.dropConstraints(
                writer, oldSchema, newSchema, true, searchPathHelper
            )
            PgDiffConstraints.dropConstraints(
                writer, oldSchema, newSchema, false, searchPathHelper
            )
            PgDiffIndexes.dropIndexes(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffTables.dropClusters(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffTables.dropTables(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffSequences.dropSequences(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffPolicies.dropPolicies(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffSequences.createSequences(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffSequences.alterSequences(
                writer, arguments, oldSchema, newSchema, searchPathHelper
            )
            PgDiffTypes.alterTypes(writer, arguments, oldSchema, newSchema, searchPathHelper)
            PgDiffTypes.createTypes(writer, oldSchema, newSchema, searchPathHelper)
            PgDiffTypes.dropTypes(writer, oldSchema, newSchema, searchPathHelper)
            PgDiffGrant.createGrants(writer, oldSchema, newSchema)
            PgDiffTables.createTables(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffTables.alterTables(
                writer, arguments, oldSchema, newSchema, searchPathHelper
            )
            PgDiffSequences.alterCreatedSequences(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffFunctions.createFunctions(
                writer, arguments, oldSchema, newSchema, searchPathHelper
            )
            PgDiffConstraints.createConstraints(
                writer, oldSchema, newSchema, true, searchPathHelper
            )
            PgDiffConstraints.createConstraints(
                writer, oldSchema, newSchema, false, searchPathHelper
            )
            PgDiffIndexes.createIndexes(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffTables.createClusters(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffTriggers.createTriggers(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffTriggers.disableOrEnableTriggers(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffViews.createViews(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffRules.createRules(writer, oldSchema, newSchema, searchPathHelper)
            PgDiffViews.alterViews(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffPolicies.createPolicies(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffPolicies.alterPolicies(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffFunctions.alterComments(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffConstraints.alterComments(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffIndexes.alterComments(
                writer, oldSchema, newSchema, searchPathHelper
            )
            PgDiffTriggers.alterComments(
                writer, oldSchema, newSchema, searchPathHelper
            )
        }
    }
}