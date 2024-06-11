package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.schema.PgSchema
import kotlin.text.StringBuilder

class PgDiffDomains(
    val newSchema: PgSchema,
    val oldSchema: PgSchema?,
    val writer: StringBuilder
) {

    operator fun invoke() {
        alter()
        create()
    }

    fun alter() {
        if (oldSchema == null) return
        for (new in newSchema.domains) {
            val old = oldSchema.domains.get(new.name) ?: continue
            if (new.owner != old.owner) {
                new.ownerSQL(writer)
            }
            if (new.comment != old.comment) {
                new.commentSQL(writer)
            }

            if (new.notNull != old.notNull) {
                if (new.notNull) {
                    new.alterSQL(writer, "SET NOT NULL;")
                } else {
                    new.alterSQL(writer, "DROP NOT NULL;")
                }
            }

            if (new.default != old.default) {
                if (new.default.isNullOrEmpty()) {
                    new.alterSQL(writer, "DROP DEFAULT;")
                } else {
                    new.alterSQL(writer, "SET DEFAULT ${new.default};")
                }
            }

            // ALTER DOMAIN empty_domain ADD CONSTRAINT hoschi1 CHECK (VALUE is  not null);
            if (new.constraints != old.constraints) {
                old.constraints.forEach {
                    if (!new.constraints.contains(it)) {
                        old.alterSQL(writer, "DROP CONSTRAINT ${it.quotedIdentifier()};")
                    }
                }
                new.constraints.forEach {
                    if (!old.constraints.contains(it)) {
                        new.alterSQL(writer, "ADD ${it.sql()};")
                    }
                }
            }
            if (new.dataType != old.dataType ||
                new.collation != old.collation
            ) {
                TODO("alter type or collation not supported ${new.name}")
            }
        }
    }

    fun create() {
        for (new in newSchema.domains) {
            val old = oldSchema?.domains?.get(new.name)
            if (old == null) {
                writer.println()
                new.creationSQL(writer)
            }
        }
    }

}