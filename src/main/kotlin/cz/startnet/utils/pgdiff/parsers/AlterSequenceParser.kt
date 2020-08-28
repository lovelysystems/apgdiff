/**
 * Copyright 2010 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.Resources
import java.text.MessageFormat

/**
 * Parses ALTER SEQUENCE statements.
 *
 * @author mix86
 */
object AlterSequenceParser : PatternBasedSubParser(
    "^ALTER[\\s]+SEQUENCE[\\s]+.*$",

    ) {
    /**
     * Parses ALTER SEQUENCE statement.
     *
     * @param database                database
     * @param statement               ALTER SEQUENCE statement
     * @param outputIgnoredStatements whether ignored statements should be
     * output in the diff
     */
    override fun parse(parser: Parser, ctx: ParserContext) {
        val parser = Parser(parser.string)
        parser.expect("ALTER", "SEQUENCE")
        val sequenceName = parser.parseIdentifier()
        val schemaName = ParserUtils.getSchemaName(sequenceName, ctx.database)
        val schema = ctx.database.getSchema(schemaName)
            ?: throw RuntimeException(
                MessageFormat.format(
                    Resources.getString("CannotFindSchema"), schemaName,
                    parser.string
                )
            )
        val objectName = ParserUtils.getObjectName(sequenceName)
        val sequence = schema.getSequence(objectName)
            ?: throw RuntimeException(
                MessageFormat.format(
                    Resources.getString("CannotFindSequence"), sequenceName,
                    parser.string
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