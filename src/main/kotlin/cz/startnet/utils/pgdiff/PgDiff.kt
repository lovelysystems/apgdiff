/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.loader.PgDumpLoader
import java.io.BufferedReader
import java.io.PrintWriter

object PgDiff {

    /**
     * Creates diff on the two database schemas.
     */
    fun createDiff(
        writer: PrintWriter,
        arguments: PgDiffOptions,
        oldReader: BufferedReader,
        newReader: BufferedReader
    ) {
        val oldDatabase = PgDumpLoader.loadDatabaseSchema(
            oldReader,
            arguments.isOutputIgnoredStatements,
            arguments.isIgnoreSlonyTriggers,
            arguments.isIgnoreSchemaCreation
        )
        val newDatabase = PgDumpLoader.loadDatabaseSchema(
            newReader,
            arguments.isOutputIgnoredStatements,
            arguments.isIgnoreSlonyTriggers,
            arguments.isIgnoreSchemaCreation
        )
        val diffDB = PgDiffDatabases(writer, arguments, oldDatabase, newDatabase)
        diffDB()
    }
}