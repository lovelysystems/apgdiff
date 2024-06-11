package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.schema.PgOperator
import cz.startnet.utils.pgdiff.schema.PgSchema
import kotlin.text.StringBuilder

class PgDiffOperators(
    val newSchema: PgSchema,
    val oldSchema: PgSchema?,
    val writer: StringBuilder
) {

    operator fun invoke() {
        if (oldSchema == null) {
            create(newSchema.operators)
        } else {
            val toCreate = mutableListOf<PgOperator>()
            val toDrop = mutableListOf<PgOperator>()
            newSchema.operators.forEach { new ->
                val old = oldSchema.getOperator(new.signature)
                if (old == null) {
                    toCreate.add(new)
                } else if (!old.sameSettings(new)) {
                    // TODO: implement ALTER OPERATOR
                    toDrop.add(old)
                    toCreate.add(new)
                }
            }
            if (toDrop.isNotEmpty() || toCreate.isNotEmpty()) {
                drop(toDrop)
                create(toCreate)
            }
        }
    }

    fun create(operators: Collection<PgOperator>) {
        operators.forEach {
            writer.println()
            it.creationSQL(writer)
            it.ownerSQL(writer)
            if (it.comment != null) {
                it.commentSQL(writer)
            }
        }
    }

    fun drop(operators: Collection<PgOperator>) {
        operators.forEach {
            writer.println()
            writer.println(it.dropSQL)
        }
    }
}