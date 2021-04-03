package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.schema.PgDatabase
import java.io.PrintWriter

class PgDiffDatabases(
    private val writer: PrintWriter,
    private val arguments: PgDiffArguments,
    private val oldDatabase: PgDatabase,
    private val newDatabase: PgDatabase
) {

    /**
     * Creates diff from comparison of two database schemas.
     */
    operator fun invoke() {
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
        createNewSchemas()
        dropOldExtensions()
        createNewExtensions()
        updateSchemas()
        dropOldSchemas()
        if (arguments.isAddTransaction) {
            writer.println()
            writer.println("COMMIT TRANSACTION;")
        }
        if (arguments.isOutputIgnoredStatements) {
            if (oldDatabase.ignoredStatements.isNotEmpty()) {
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
            if (newDatabase.ignoredStatements.isNotEmpty()) {
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
     * Creates new schemas (not the objects inside the schemas).
     */
    private fun createNewSchemas() {
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
     */
    private fun createNewExtensions() {
        for (newExtension in newDatabase.extensions) {
            if (oldDatabase.getExtension(newExtension.name) == null) {
                writer.println()
                writer.println(newExtension.creationSQL)
            }
        }
    }


    /**
     * Drops old schemas that do not exist anymore.
     */
    private fun dropOldSchemas() {
        for (oldSchema in oldDatabase.schemas) {
            if (newDatabase.getSchema(oldSchema.name) == null) {
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
     */
    private fun dropOldExtensions() {
        for (oldExtension in oldDatabase.extensions) {
            if (newDatabase.getExtension(oldExtension.name) == null) {
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
     */
    private fun updateSchemas() {
        val setSearchPath = (newDatabase.schemas.size > 1
                || newDatabase.schemas[0].name != "public")
        for (newSchema in newDatabase.schemas) {
            val searchPathHelper: SearchPathHelper = if (setSearchPath) {
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

            PgDiffDomains(newSchema, oldSchema, writer, searchPathHelper)()
            PgDiffOperators(newSchema, oldSchema, writer, searchPathHelper)()

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