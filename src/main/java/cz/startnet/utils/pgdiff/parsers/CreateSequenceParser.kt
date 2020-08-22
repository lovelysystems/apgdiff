/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.Resources
import cz.startnet.utils.pgdiff.schema.PgDatabase
import cz.startnet.utils.pgdiff.schema.PgSequence
import java.text.MessageFormat

/**
 * Parses CREATE SEQUENCE statements.
 *
 * @author fordfrog
 */
object CreateSequenceParser {
    /**
     * Parses CREATE SEQUENCE statement.
     *
     * @param database  database
     * @param statement CREATE SEQUENCE statement
     */
    fun parse(
        database: PgDatabase,
        statement: String
    ) {
        val parser = Parser(statement)
        parser.expect("CREATE", "SEQUENCE")
        val sequenceName = parser.parseIdentifier()
        val sequence = PgSequence(ParserUtils.getObjectName(sequenceName))
        val schemaName = ParserUtils.getSchemaName(sequenceName, database)
        val schema = database.getSchema(schemaName)
                ?: throw RuntimeException(
                    MessageFormat.format(
                        Resources.getString("CannotFindSchema"), schemaName,
                        statement
                    )
                )
        schema.addSequence(sequence)
        while (!parser.expectOptional(";")) {
            if (parser.expectOptional("AS")) {
                sequence.dataType = parser.parseString()
            } else if (parser.expectOptional("INCREMENT")) {
                parser.expectOptional("BY")
                sequence.increment = parser.parseString()
            } else if (parser.expectOptional("MINVALUE")) {
                sequence.minValue = parser.parseString()
            } else if (parser.expectOptional("MAXVALUE")) {
                sequence.maxValue = parser.parseString()
            } else if (parser.expectOptional("START")) {
                parser.expectOptional("WITH")
                sequence.startWith = parser.parseString()
            } else if (parser.expectOptional("CACHE")) {
                sequence.cache = parser.parseString()
            } else if (parser.expectOptional("CYCLE")) {
                sequence.isCycle = true
            } else if (parser.expectOptional("OWNED", "BY")) {
                if (parser.expectOptional("NONE")) {
                    sequence.ownedBy = null
                } else {
                    sequence.ownedBy = ParserUtils.getObjectName(
                        parser.parseIdentifier()
                    )
                }
            } else if (parser.expectOptional("NO")) {
                if (parser.expectOptional("MINVALUE")) {
                    sequence.minValue = null
                } else if (parser.expectOptional("MAXVALUE")) {
                    sequence.maxValue = null
                } else if (parser.expectOptional("CYCLE")) {
                    sequence.isCycle = false
                } else {
                    parser.throwUnsupportedCommand()
                }
            } else {
                parser.throwUnsupportedCommand()
            }
        }
    }
}