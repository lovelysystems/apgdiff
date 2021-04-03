package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.Resources
import java.text.MessageFormat

object CommentParser : PatternBasedSubParser(
    "^COMMENT[\\s]+ON[\\s]+.*$"
) {

    val subParsers = listOf(
        "EXTENSION" to ::parseExtension,
        "TABLE" to ::parseTable,
        "TYPE" to ::parseType,
        "DOMAIN" to ::parseDomain,
        "COLUMN" to ::parseColumn,
        "CONSTRAINT" to ::parseConstraint,
        "DATABASE" to ::parseDatabase,
        "FUNCTION" to ::parseFunction,
        "OPERATOR" to ::parseOperator,
        "INDEX" to ::parseIndex,
        "SCHEMA" to ::parseSchema,
        "SEQUENCE" to ::parseSequence,
        "TRIGGER" to ::parseTrigger,
        "VIEW" to ::parseView,
    )

    override fun parse(parser: Parser, ctx: ParserContext) {
        parser.expect("COMMENT", "ON")
        val cp = subParsers.firstOrNull {
            parser.expectOptional(it.first)
        }
        if (cp != null) {
            cp.second(parser, ctx)
        } else {
            ctx.database.addIgnoredStatement(parser.string)
        }
    }

    private fun parseExtension(parser: Parser, ctx: ParserContext) {
        val name = parser.parseIdentifier()
        val o = ctx.database.getExtension(name)!!
        parser.expect("IS")
        o.comment = getComment(parser)
        parser.expect(";")
    }

    private fun parseTable(parser: Parser, ctx: ParserContext) {
        val tableName = parser.parseIdentifier()
        val objectName = ParserUtils.getObjectName(tableName)
        val schemaName = ParserUtils.getSchemaName(tableName, ctx.database)
        val table = ctx.database.getSchema(schemaName)!!.getTable(objectName)!!
        parser.expect("IS")
        table.comment = getComment(parser)
        parser.expect(";")
    }

    private fun parseType(parser: Parser, ctx: ParserContext) {
        val objectName = ctx.database.getSchemaObjectName(parser.parseIdentifier())
        val type = ctx.database.getSchema(objectName).getType(objectName.name)
            ?: error("type $objectName not found")
        parser.expect("IS")
        type.comment = getComment(parser)
        parser.expect(";")
    }

    private fun parseDomain(parser: Parser, ctx: ParserContext) {
        val objectName = ctx.database.getSchemaObjectName(parser.parseIdentifier())
        val domain = ctx.database.getSchema(objectName).domains.get(objectName.name)
            ?: error("domain $objectName not found")
        parser.expect("IS")
        domain.comment = getComment(parser)
        parser.expect(";")
    }

    private fun parseConstraint(parser: Parser, ctx: ParserContext) {
        val constraintName = ParserUtils.getObjectName(parser.parseIdentifier())
        parser.expect("ON")
        val tableName = parser.parseIdentifier()
        val objectName = ParserUtils.getObjectName(tableName)
        val schemaName = ParserUtils.getSchemaName(tableName, ctx.database)
        val constraint = ctx.database.getSchema(schemaName)!!.getTable(objectName)!!.getConstraint(constraintName)!!
        parser.expect("IS")
        constraint.comment = getComment(parser)
        parser.expect(";")
    }

    private fun parseDatabase(parser: Parser, ctx: ParserContext) {
        parser.parseIdentifier()
        parser.expect("IS")
        ctx.database.comment = getComment(parser)
        parser.expect(";")
    }

    private fun parseIndex(parser: Parser, ctx: ParserContext) {
        val indexName = parser.parseIdentifier()
        val objectName = ParserUtils.getObjectName(indexName)
        val schemaName = ParserUtils.getSchemaName(indexName, ctx.database)
        val schema = ctx.database.getSchema(schemaName)
        val index = schema!!.getIndex(objectName)
        if (index == null) {
            val primaryKey = schema.getPrimaryKey(objectName)!!
            parser.expect("IS")
            primaryKey.comment = getComment(parser)
            parser.expect(";")
        } else {
            parser.expect("IS")
            index.comment = getComment(parser)
            parser.expect(";")
        }
    }

    private fun parseSchema(parser: Parser, ctx: ParserContext) {
        val schemaName = ParserUtils.getObjectName(parser.parseIdentifier())
        val schema = ctx.database.getSchema(schemaName)!!
        parser.expect("IS")
        schema.comment = getComment(parser)
        parser.expect(";")
    }

    private fun parseSequence(parser: Parser, ctx: ParserContext) {
        val sequenceName = parser.parseIdentifier()
        val objectName = ParserUtils.getObjectName(sequenceName)
        val schemaName = ParserUtils.getSchemaName(sequenceName, ctx.database)
        val sequence = ctx.database.getSchema(schemaName)!!.getSequence(objectName)!!
        parser.expect("IS")
        sequence.comment = getComment(parser)
        parser.expect(";")
    }

    private fun parseTrigger(parser: Parser, ctx: ParserContext) {
        val triggerName = ParserUtils.getObjectName(parser.parseIdentifier())
        parser.expect("ON")
        val tableName = parser.parseIdentifier()
        val objectName = ParserUtils.getObjectName(tableName)
        val schemaName = ParserUtils.getSchemaName(tableName, ctx.database)
        val trigger = ctx.database.getSchema(schemaName)!!.getTable(objectName)!!.getTrigger(triggerName)!!
        parser.expect("IS")
        trigger.comment = getComment(parser)
        parser.expect(";")
    }

    private fun parseView(parser: Parser, ctx: ParserContext) {
        val viewName = parser.parseIdentifier()
        val objectName = ParserUtils.getObjectName(viewName)
        val schemaName = ParserUtils.getSchemaName(viewName, ctx.database)
        val view = ctx.database.getSchema(schemaName)!!.getView(objectName)!!
        parser.expect("IS")
        view.comment = getComment(parser)
        parser.expect(";")
    }

    private fun parseColumn(parser: Parser, ctx: ParserContext) {
        val columnName = parser.parseIdentifier()
        val objectName = ParserUtils.getObjectName(columnName)
        val relName = ParserUtils.getSecondObjectName(columnName)
        val schemaName = ParserUtils.getThirdObjectName(columnName)
        val schema = ctx.database.getSchema(schemaName)
        val rel = schema!!.getRelation(relName)
        val column = rel!!.getColumn(objectName)
            ?: throw ParserException(
                MessageFormat.format(
                    Resources.getString("CannotFindColumnInTable"),
                    columnName, rel.name
                )
            )
        parser.expect("IS")
        column.comment = getComment(parser)
        parser.expect(";")
    }

    private fun parseFunction(parser: Parser, ctx: ParserContext) {
        val tmpFunction = parser.parseFunctionSignature(ctx)
        val schema = ctx.database.getSchema(tmpFunction.schema)
        val function = schema!!.getFunction(tmpFunction.signature)!!
        parser.expect("IS")
        function.comment = getComment(parser)
        parser.expect(";")
    }

    private fun parseOperator(parser: Parser, ctx: ParserContext) {
        val operator = parser.parseOperatorSignature(ctx)
        parser.expect("IS")
        operator.comment = getComment(parser)
        parser.expect(";")
    }

    /**
     * Parses comment from parser. If comment is "null" string then null is
     * returned, otherwise the parsed string is returned.
     *
     * @param parser parser
     *
     * @return string or null
     */
    private fun getComment(parser: Parser): String? {
        val comment = parser.parseString()
        return if ("null".equals(comment, ignoreCase = true)) {
            null
        } else comment
    }
}