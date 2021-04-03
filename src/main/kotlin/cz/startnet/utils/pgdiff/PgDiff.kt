/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.loader.PgDumpLoader
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
        val diffDB = PgDiffDatabases(writer, arguments, oldDatabase, newDatabase)
        diffDB()
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
        arguments: PgDiffArguments,
        oldInputStream: InputStream?,
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
        val diffDB = PgDiffDatabases(writer, arguments, oldDatabase, newDatabase)
        diffDB()
    }
}