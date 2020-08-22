/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.schema.PgDatabase
import cz.startnet.utils.pgdiff.schema.PgTrigger
import cz.startnet.utils.pgdiff.schema.PgTrigger.EventTimeQualification

/**
 * Parses CREATE TRIGGER statements.
 *
 * @author fordfrog
 */
object CreateTriggerParser {
    /**
     * Parses CREATE TRIGGER statement.
     *
     * @param database            database
     * @param statement           CREATE TRIGGER statement
     * @param ignoreSlonyTriggers whether Slony triggers should be ignored
     */
    fun parse(
        database: PgDatabase,
        statement: String, ignoreSlonyTriggers: Boolean
    ) {
        val parser = Parser(statement)
        parser.expect("CREATE", "TRIGGER")
        val triggerName = parser.parseIdentifier()
        val objectName = ParserUtils.getObjectName(triggerName)
        val trigger = PgTrigger()
        trigger.name = objectName
        if (parser.expectOptional("BEFORE")) {
            trigger.eventTimeQualification = EventTimeQualification.before
        } else if (parser.expectOptional("AFTER")) {
            trigger.eventTimeQualification = EventTimeQualification.after
        } else if (parser.expectOptional("INSTEAD OF")) {
            trigger.eventTimeQualification = EventTimeQualification.instead_of
        }
        var first = true
        while (true) {
            if (!first && !parser.expectOptional("OR")) {
                break
            } else if (parser.expectOptional("INSERT")) {
                trigger.isOnInsert = true
            } else if (parser.expectOptional("UPDATE")) {
                trigger.isOnUpdate = true
                if (parser.expectOptional("OF")) {
                    do {
                        trigger.addUpdateColumn(parser.parseIdentifier())
                    } while (parser.expectOptional(","))
                }
            } else if (parser.expectOptional("DELETE")) {
                trigger.isOnDelete = true
            } else if (parser.expectOptional("TRUNCATE")) {
                trigger.isOnTruncate = true
            } else if (first) {
                break
            } else {
                parser.throwUnsupportedCommand()
            }
            first = false
        }
        parser.expect("ON")
        val relationName = parser.parseIdentifier()
        trigger.relationName = ParserUtils.getObjectName(relationName)
        val referencing = "REFERENCING"
        if (parser.expectOptional(referencing)) {
            trigger.referencing = "\t" + referencing
            while (parser.getSubString(
                    parser.position - 5,
                    parser.position - 4
                ) != System.getProperty("line.separator")
            ) {
                parseReferencing(parser, trigger)
            }
        }
        if (parser.expectOptional("FOR")) {
            parser.expectOptional("EACH")
            if (parser.expectOptional("ROW")) {
                trigger.isForEachRow = true
            } else if (parser.expectOptional("STATEMENT")) {
                trigger.isForEachRow = false
            } else {
                parser.throwUnsupportedCommand()
            }
        }
        if (parser.expectOptional("WHEN")) {
            parser.expect("(")
            trigger.`when` =  parser.expression
            parser.expect(")")
        }
        parser.expect("EXECUTE")
        parser.expectOptional("PROCEDURE")
        trigger.function = parser.rest
        val ignoreSlonyTrigger = (ignoreSlonyTriggers
                && ("_slony_logtrigger" == trigger.name || "_slony_denyaccess" == trigger.name))
        if (!ignoreSlonyTrigger) {
            val schema = database.getSchema(
                ParserUtils.getSchemaName(relationName, database)
            )
            schema!!.getRelation(trigger.relationName)!!.addTrigger(trigger)
        }
    }

    private fun parseReferencing(parser: Parser, trigger: PgTrigger) {
        if (parser.expectOptional("NEW")) {
            trigger.referencing = trigger.referencing + " NEW "
        }
        if (parser.expectOptional("OLD")) {
            trigger.referencing = trigger.referencing + " OLD "
        }
        parser.expect("TABLE")
        parser.expect("AS")
        trigger.referencing = trigger.referencing + "TABLE AS " + parser.parseString()
    }

    fun parseDisable(
        database: PgDatabase,
        statement: String
    ) {
        val parser = Parser(statement)
        parser.expect("ALTER", "TABLE")
        val tableName = parser.parseIdentifier()
        parser.expect("DISABLE", "TRIGGER")
        val objectName = parser.parseIdentifier()
        val trigger = PgTrigger()
        trigger.name = objectName
        trigger.relationName = ParserUtils.getObjectName(tableName)
        val schema = database.getSchema(
            ParserUtils.getSchemaName(tableName, database)
        )
        schema!!.getRelation(trigger.relationName)!!.getTrigger(objectName)!!.isDisable = true
    }
}