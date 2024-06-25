package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.schema.*

/**
 * A visitor that prints drop statements for any objects that are absent in the new database
 */
class DropObjectsVisitor(
    private val newDB: PgDatabase,
    val writer: StringBuilder,
    val options: PgDiffOptions,
) : WalkingVisitor() {

    lateinit var newSchema: PgSchema

    override fun visit(o: PgSchema) {
        if (!options.schemaIncluded(o.name)) return
        newDB.getSchema(o.name)?.also {
            newSchema = it
            super.visit(o)
        } ?: drop(o)
    }

    override fun visit(o: PgFunction) {
        if (!newSchema.containsFunction(o.signature)) {
            drop(o)
        }
    }

    override fun visit(o: PgViewBase) {
        if (newSchema.getView(o.name) == null) {
            drop(o)
        }
    }

    override fun visit(o: PgTableBase) {
        val newTable = newSchema.getTable(o.name)
        if (newTable == null) {
            drop(o)
            return
        }
        for (rule in o.rules) {
            if (newTable.rules.find { it.name == rule.name } == null) {
                drop(rule)
            }
        }
    }

    override fun visit(o: PgSequence) {
        if (!newSchema.containsSequence(o.name)) {
            drop(o)
        }
    }

    override fun visit(o: PgType) {
        if (!newSchema.containsType(o.name)) {
            drop(o)
        }
    }

    override fun visit(o: PgDomain) {
        if (!newSchema.domains.containsSame(o)) {
            drop(o)
        }
    }

    override fun visit(o: PgOperator) {
        if (newSchema.getOperator(o.signature) == null) {
            drop(o)
        }
    }

    private fun fqn(quotedName: String): String {
        return "${PgDiffUtils.getQuotedName(schema.name)}.$quotedName"
    }

    private fun drop(o: DBObject) {
        printDropStmt(o.objectType, "IF EXISTS", fqn(o.quotedIdentifier()))
    }

    private fun drop(o: PgFunction) {
        printDropStmt("FUNCTION IF EXISTS", fqn(o.signatureSQL))
    }

    private fun drop(o: PgRule) {
        printDropStmt(
            "RULE IF EXISTS", PgDiffUtils.getQuotedName(o.name), "ON", o.relationName.toString()
        )
    }

    private fun drop(o: PgSchema) {
        // always cascade because we want to drop everything contained in the schema
        writer.printStmt("DROP SCHEMA IF EXISTS", PgDiffUtils.getQuotedName(o.name), "CASCADE")
    }

    private fun printDropStmt(vararg parts: String) {
        if (options.dropCascade)
            writer.printStmt("DROP", *parts, "CASCADE")
        else
            writer.printStmt("DROP", *parts)
    }


}
