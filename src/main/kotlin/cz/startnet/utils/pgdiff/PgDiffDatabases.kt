package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.schema.PgDatabase
import java.io.ByteArrayOutputStream


class PgDiffDatabases(
    private val writer: DiffWriter,
    private val arguments: PgDiffOptions,
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
        commentExtensions()
        updateSchemas()

        val dropObjectsVisitor = DropObjectsVisitor(newDatabase, writer, arguments)
        dropObjectsVisitor.accept(oldDatabase)

        if (arguments.isAddTransaction) {
            writer.println()
            writer.println("COMMIT TRANSACTION;")
        }
        if (arguments.outputIgnoredStatements) {
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
            if (!arguments.schemaIncluded(newSchema.name)) continue
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

    private fun commentExtensions() {
        for (newExtension in newDatabase.extensions) {
            if (newExtension.comment != oldDatabase.getExtension(newExtension.name)?.comment) {
                newExtension.commentSQL(writer)
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
        // order schemas by positions of the first relation, to order by schema dependencies
        // TODO: a more safe way would be create all objects based on their position
        val schemas = newDatabase.schemas.sortedBy {
            if (it.rels.isNotEmpty()) {
                it.rels.minOf { it.position }
            } else {
                0
            }
        }
        for (newSchema in schemas) {
            if (!arguments.schemaIncluded(newSchema.name)) continue
            val diff = ByteArrayOutputStream()
            val schemaWriter = DiffWriter(diff, arguments)
            val oldSchema = oldDatabase.getSchema(newSchema.name)
            if (oldSchema != null) {
                if (oldSchema.comment == null
                    && newSchema.comment != null
                    || oldSchema.comment != null && newSchema.comment != null && oldSchema.comment != newSchema.comment
                ) {
                    schemaWriter.println()
                    schemaWriter.printStmt(
                        "COMMENT ON SCHEMA",
                        PgDiffUtils.getQuotedName(newSchema.name),
                        "IS",
                        newSchema.comment!!
                    )
                } else if (oldSchema.comment != null
                    && newSchema.comment == null
                ) {
                    schemaWriter.println()
                    schemaWriter.print("COMMENT ON SCHEMA ")
                    schemaWriter.print(
                        PgDiffUtils.getQuotedName(newSchema.name)
                    )
                    schemaWriter.println(" IS NULL;")
                }
            }
            PgDiffTriggers.dropTriggers(
                schemaWriter, oldSchema, newSchema
            )
            PgDiffViews.dropViews(
                schemaWriter, oldSchema, newSchema
            )
            PgDiffConstraints.dropConstraints(
                schemaWriter, oldSchema, newSchema, true
            )
            PgDiffConstraints.dropConstraints(
                schemaWriter, oldSchema, newSchema, false
            )
            PgDiffIndexes.dropIndexes(
                schemaWriter, oldSchema, newSchema
            )
            PgDiffTables.dropClusters(
                schemaWriter, oldSchema, newSchema
            )
            PgDiffPolicies.dropPolicies(
                schemaWriter, oldSchema, newSchema
            )
            PgDiffSequences.createSequences(
                schemaWriter, oldSchema, newSchema
            )
            PgDiffSequences.alterSequences(
                schemaWriter, oldSchema, newSchema
            )
            PgDiffTypes.alterTypes(schemaWriter, arguments, oldSchema, newSchema)
            PgDiffTypes.createTypes(schemaWriter, oldSchema, newSchema)
            // PgDiffTypes.dropTypes(schemaWriter, oldSchema, newSchema)

            PgDiffDomains(newSchema, oldSchema, schemaWriter)()
            PgDiffOperators(newSchema, oldSchema, schemaWriter)()

            PgDiffGrant.createGrants(schemaWriter, oldSchema, newSchema)
            PgDiffTables.createTables(
                schemaWriter, oldSchema, newSchema
            )
            PgDiffTables.alterTables(
                schemaWriter, arguments, oldSchema, newSchema
            )
            PgDiffSequences.alterCreatedSequences(
                schemaWriter, oldSchema, newSchema
            )
            PgDiffFunctions.createFunctions(
                schemaWriter, arguments, oldSchema, newSchema
            )
            PgDiffConstraints.createConstraints(
                schemaWriter, oldSchema, newSchema, true
            )
            PgDiffConstraints.createConstraints(
                schemaWriter, oldSchema, newSchema, false
            )
            PgDiffIndexes.createIndexes(
                schemaWriter, oldSchema, newSchema
            )
            PgDiffTables.createClusters(
                schemaWriter, oldSchema, newSchema
            )
            PgDiffTriggers.createTriggers(
                schemaWriter, oldSchema, newSchema
            )
            PgDiffTriggers.disableOrEnableTriggers(
                schemaWriter, oldSchema, newSchema
            )
            PgDiffViews.createViews(
                schemaWriter, oldSchema, newSchema
            )
            PgDiffRules.createRules(schemaWriter, oldSchema, newSchema)
            PgDiffViews.alterViews(
                schemaWriter, oldSchema, newSchema
            )
            PgDiffPolicies.createPolicies(
                schemaWriter, oldSchema, newSchema
            )
            PgDiffPolicies.alterPolicies(
                schemaWriter, oldSchema, newSchema
            )
            PgDiffFunctions.alterComments(
                schemaWriter, oldSchema, newSchema
            )
            PgDiffConstraints.alterComments(
                schemaWriter, oldSchema, newSchema
            )
            PgDiffIndexes.alterComments(
                schemaWriter, oldSchema, newSchema
            )
            PgDiffTriggers.alterComments(
                schemaWriter, oldSchema, newSchema
            )

            schemaWriter.flush()
            val diffString = diff.toString(arguments.outCharsetName)
            if (diffString.isNotEmpty()) {
                val setSearchPath = (newDatabase.schemas.size > 1
                        || newDatabase.schemas[0].name != "public")
                val searchPathHelper: SearchPathHelper = if (setSearchPath) {
                    SearchPathHelper(
                        "SET search_path = "
                                + PgDiffUtils.getQuotedName(newSchema.name, true)
                                + ", pg_catalog;"
                    )
                } else {
                    SearchPathHelper(null)
                }
                searchPathHelper.outputSearchPath(writer)
                writer.write(diffString)
            }
        }
    }
}