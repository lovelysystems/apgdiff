package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.schema.DomainConstraint
import cz.startnet.utils.pgdiff.schema.PgDomain


/**
 * https://www.postgresql.org/docs/12/sql-createdomain.html
 * CREATE DOMAIN name [ AS ] data_type [ COLLATE collation ] [ DEFAULT expression ] [ constraint [ ... ] ]
 * where constraint is:
 * [ CONSTRAINT constraint_name ]
 *   { NOT NULL | NULL | CHECK (expression) }
 */
object CreateDomainParser : PatternBasedSubParser(
    "^CREATE[\\s]+DOMAIN[\\s]+.*$"
) {
    override fun parse(parser: Parser, ctx: ParserContext) {
        parser.expect("CREATE", "DOMAIN")
        val objectName = ctx.database.getSchemaObjectName(parser.parseIdentifier())
        val schema = ctx.database.getSchema(objectName)
        val domain = PgDomain(objectName.name)
        schema.domains.add(domain)
        parser.expectOptional("AS")
        domain.dataType = parser.parseIdentifier()
        if (parser.expectOptional("COLLATE")) {
            domain.collation = parser.parseIdentifier()
        }
        if (parser.expectOptional("DEFAULT")) {
            domain.default = parser.expression
        }
        if (parser.expectOptional("NOT NULL")) {
            domain.notNull = true
        } else {
            parser.expectOptional("NULL")
        }

        while (parser.expectOptional("CONSTRAINT")) {
            val name = parser.parseIdentifier()
            parser.expect("CHECK")
            val check = parser.expression
            domain.constraints.add(DomainConstraint(name, check))
        }
    }
}
