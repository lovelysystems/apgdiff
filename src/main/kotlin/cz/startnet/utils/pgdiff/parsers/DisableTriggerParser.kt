package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.schema.PgTrigger

/**
 * Parses ALTER TABLE ... DISABLE TRIGGER statements.
 */
object DisableTriggerParser : PatternBasedSubParser(
    "ALTER\\s+TABLE+\\s+\\w+.+\\w+\\s+DISABLE+\\s+TRIGGER+\\s+\\w+.*$"
) {

    override fun parse(parser: Parser, ctx: ParserContext) {
        parser.expect("ALTER", "TABLE")
        val tableName = parser.parseIdentifier()
        parser.expect("DISABLE", "TRIGGER")
        val objectName = parser.parseIdentifier()
        val trigger = PgTrigger()
        trigger.name = objectName
        trigger.relationName = ParserUtils.getObjectName(tableName)
        val schema = ctx.database.getSchema(
            ParserUtils.getSchemaName(tableName, ctx.database)
        )
        schema!!.getRelation(trigger.relationName)!!.getTrigger(objectName)!!.isDisable = true
    }
}