/**
 * Copyright 2010 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.Resources
import cz.startnet.utils.pgdiff.schema.PgDatabase
import java.text.MessageFormat

/**
 * Parses ALTER SEQUENCE statements.
 *
 * @author mix86
 */
object AlterSequenceParser {
    /**
     * Parses ALTER SEQUENCE statement.
     *
     * @param database                database
     * @param statement               ALTER SEQUENCE statement
     * @param outputIgnoredStatements whether ignored statements should be
     * output in the diff
     */
    fun parse(
        database: PgDatabase,
        statement: String, outputIgnoredStatements: Boolean
    ) {
        val parser = Parser(statement)
        parser.expect("ALTER", "SEQUENCE")
        val sequenceName = parser.parseIdentifier()
        val schemaName = ParserUtils.getSchemaName(sequenceName, database)
        val schema = database.getSchema(schemaName)
                ?: throw RuntimeException(
                    MessageFormat.format(
                        Resources.getString("CannotFindSchema"), schemaName,
                        statement
                    )
                )
        val objectName = ParserUtils.getObjectName(sequenceName)
        val sequence = schema.getSequence(objectName)
                ?: throw RuntimeException(
                    MessageFormat.format(
                        Resources.getString("CannotFindSequence"), sequenceName,
                        statement
                    )
                )
        while (!parser.expectOptional(";")) {
            if (parser.expectOptional("OWNED", "BY")) {
                if (parser.expectOptional("NONE")) {
                    sequence.ownedBy = null
                } else {
                    sequence.ownedBy = parser.expression
                }
            } else {
                parser.throwUnsupportedCommand()
            }
        }
    }
}