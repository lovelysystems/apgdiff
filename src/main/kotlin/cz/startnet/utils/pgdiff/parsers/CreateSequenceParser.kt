package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.Resources
import cz.startnet.utils.pgdiff.schema.PgSequence
import java.text.MessageFormat

/**
 * Parses CREATE SEQUENCE statements.
 */
object CreateSequenceParser : PatternBasedSubParser(
    "^CREATE[\\s]+SEQUENCE[\\s]+.*$"
) {
    override fun parse(parser: Parser, ctx: ParserContext) {
        parser.expect("CREATE", "SEQUENCE")
        val sequenceName = parser.parseIdentifier()
        val sequence = PgSequence(ParserUtils.getObjectName(sequenceName), parser.statementNum)
        val schemaName = ParserUtils.getSchemaName(sequenceName, ctx.database)
        val schema = ctx.database.getSchema(schemaName)
            ?: throw RuntimeException(
                MessageFormat.format(
                    Resources.getString("CannotFindSchema"), schemaName,
                    parser.string
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