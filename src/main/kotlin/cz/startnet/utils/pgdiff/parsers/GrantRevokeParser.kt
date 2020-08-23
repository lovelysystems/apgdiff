/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.Resources
import cz.startnet.utils.pgdiff.schema.*
import java.text.MessageFormat
import java.util.*

/**
 * Parses GRANT statements.
 *
 * @author user
 */
object GrantRevokeParser {
    /**
     * Parses GRANT statement.
     *
     * @param database
     * database
     * @param statement
     * GRANT statement
     * @param outputIgnoredStatements
     * whether ignored statements should be output in the diff
     */
    fun parse(
        database: PgDatabase, statement: String,
        outputIgnoredStatements: Boolean
    ) {
        val grant: Boolean
        // Map<String, List<String>> privileges = new TreeMap<String,
        // List<String>>();
        val privileges: MutableList<String?> = ArrayList()
        val privilegesColumns: MutableList<List<String>?> = ArrayList()
        val identifiers: MutableList<String?> = ArrayList()
        val roles: MutableList<String?> = ArrayList()
        var grantOption = false
        val revokeMode: String?
        val parser = Parser(statement)
        grant = parser.expect("GRANT", true)
        if (!grant) {
            parser.expect("REVOKE")
            grantOption = parser.expect("GRANT OPTION FOR", true)
        }
        var privilege = parser.expectOptionalOneOf(
            "ALL", "SELECT",
            "INSERT", "UPDATE", "DELETE", "TRUNCATE", "REFERENCES",
            "TRIGGER", "USAGE"
        )
        var columns: List<String>? = null
        if (privilege == null) {
            // unknown privilege so unsupported object privilege
            // object role_name is using a different syntax so will always pass
            // here
            if (outputIgnoredStatements) {
                database.addIgnoredStatement(statement)
                return
            } else {
                return
            }
        }
        if (privilege != null && "ALL".equals(privilege, ignoreCase = true)) {
            parser.expectOptional("PRIVILEGES")
        }
        columns = if (privilege != null && "ALL".equals(privilege, ignoreCase = true) || "SELECT".equals(
                privilege,
                ignoreCase = true
            )
            || "INSERT".equals(privilege, ignoreCase = true)
            || "UPDATE".equals(privilege, ignoreCase = true)
            || "REFERENCES".equals(privilege, ignoreCase = true)
        ) {
            parseColumns(
                parser, database, statement,
                outputIgnoredStatements
            )
        } else {
            null
        }
        privileges.add(privilege)
        privilegesColumns.add(columns)
        while (privilege != null) {
            if (parser.expectOptional(",")) {
                privilege = parser.expectOptionalOneOf(
                    "SELECT", "INSERT",
                    "UPDATE", "DELETE", "TRUNCATE", "REFERENCES",
                    "TRIGGER", "USAGE"
                )
                columns = if (privilege != null && "ALL".equals(privilege, ignoreCase = true) || "SELECT".equals(
                        privilege,
                        ignoreCase = true
                    )
                    || "INSERT".equals(privilege, ignoreCase = true)
                    || "UPDATE".equals(privilege, ignoreCase = true)
                    || "REFERENCES".equals(privilege, ignoreCase = true)
                ) {
                    parseColumns(
                        parser, database, statement,
                        outputIgnoredStatements
                    )
                } else {
                    null
                }
                privileges.add(privilege)
                privilegesColumns.add(columns)
            } else {
                privilege = null
            }
        }
        val separator = parser.expectOptional("ON")
        if (!separator) {
            // column object
            if (outputIgnoredStatements) {
                database.addIgnoredStatement(statement)
                return
            }
        }

        // TODO check 'ALL TABLES IN SCHEMA' may not work
        var objectType = parser.expectOptionalOneOf(
            "TABLE",
            "ALL TABLES IN SCHEMA", "SEQUENCE", "ALL SEQUENCES IN SCHEMA",
            "DATABASE", "DOMAIN", "FOREIGN DATA WRAPPER", "FOREIGN SERVER",
            "FUNCTION", "ALL FUNCTIONS IN SCHEMA", "LANGUAGE",
            "LARGE OBJECT", "SCHEMA", "TABLESPACE", "TYPE"
        )
        if (objectType == null) {
            objectType = "TABLE"
        }
        var identifier: String? = parser.parseIdentifier()
        if ("FUNCTION".equals(objectType, ignoreCase = true)
            || "ALL FUNCTIONS IN SCHEMA".equals(objectType, ignoreCase = true)
        ) {
            parseConsumeFunctionSignature(
                parser, database, statement,
                outputIgnoredStatements
            )
        }
        identifiers.add(identifier)
        while (identifier != null) {
            if (parser.expectOptional(",")) {
                identifier = parser.parseIdentifier()
                if ("FUNCTION".equals(objectType, ignoreCase = true)
                    || "ALL FUNCTIONS IN SCHEMA"
                        .equals(objectType, ignoreCase = true)
                ) {
                    parseConsumeFunctionSignature(
                        parser, database, statement,
                        outputIgnoredStatements
                    )
                }
                identifiers.add(identifier)
            } else {
                identifier = null
            }
        }
        if (grant) {
            parser.expect("TO")
        } else {
            parser.expect("FROM")
        }
        parser.expectOptional("GROUP")
        var role: String? = parser.parseIdentifier()
        roles.add(role)
        while (role != null) {
            if (parser.expectOptional(",")) {
                parser.expectOptional("GROUP")
                role = parser.parseIdentifier()
                roles.add(role)
            } else {
                role = null
            }
        }
        if (grant) {
            grantOption = parser.expectOptional("WITH GRANT OPTION")
        } else {
            revokeMode = parser.expectOptionalOneOf("RESTRICT", "CASCADE")
            if ("CASCADE".equals(revokeMode, ignoreCase = true)) {
                if (outputIgnoredStatements) {
                    database.addIgnoredStatement(statement)
                    return
                }
            }
        }
        if ("TABLE".equals(objectType, ignoreCase = true)) {
            for (name in identifiers) {
                val schemaName = ParserUtils.getSchemaName(
                    name,
                    database
                )
                val schema = database.getSchema(schemaName)
                    ?: throw RuntimeException(
                        MessageFormat.format(
                            Resources.getString("CannotFindSchema"),
                            schemaName, statement
                        )
                    )
                val objectName = ParserUtils.getObjectName(name)
                val table = schema.getTable(objectName)
                val view = schema.getView(objectName)
                if (table == null && view == null) throw RuntimeException(
                    MessageFormat.format(
                        Resources.getString("CannotFindObject"), name,
                        statement
                    )
                )
                val rel = table ?: view!!
                for (i in privileges.indices) {
                    val privKey = privileges[i]
                    val privValue = privilegesColumns[i]
                    if (privValue != null) {
                        for (columnName in privValue) {
                            if (rel.containsColumn(columnName)) {
                                val column = rel
                                    .getColumn(columnName)
                                    ?: throw RuntimeException(
                                        MessageFormat.format(
                                            Resources.getString("CannotFindTableColumn"),
                                            columnName,
                                            rel.name, parser
                                                .string
                                        )
                                    )
                                for (roleName in roles) {
                                    var columnPrivilege = column
                                        .getPrivilege(roleName)
                                    if (columnPrivilege == null) {
                                        columnPrivilege = PgColumnPrivilege(
                                            roleName
                                        )
                                        column.addPrivilege(columnPrivilege)
                                    }
                                    columnPrivilege.setPrivileges(
                                        privKey,
                                        grant, grantOption
                                    )
                                }
                            } else {
                                throw ParserException(
                                    MessageFormat.format(
                                        Resources.getString("CannotFindColumnInTable"),
                                        columnName, rel.name
                                    )
                                )
                            }
                        }
                    } else {
                        for (roleName in roles) {
                            var relPrivilege = rel
                                .getPrivilege(roleName)
                            if (relPrivilege == null) {
                                relPrivilege = PgRelationPrivilege(roleName)
                                rel.addPrivilege(relPrivilege)
                            }
                            relPrivilege.setPrivileges(
                                privKey, grant,
                                grantOption
                            )
                        }
                    }
                }
            }
        } else if ("SEQUENCE".equals(objectType, ignoreCase = true)) {
            for (name in identifiers) {
                // final String sequenceName = parser.parseIdentifier();
                val schemaName = ParserUtils.getSchemaName(
                    name,
                    database
                )
                val schema = database.getSchema(schemaName)
                    ?: throw RuntimeException(
                        MessageFormat.format(
                            Resources.getString("CannotFindSchema"),
                            schemaName, statement
                        )
                    )
                val objectName = ParserUtils.getObjectName(name)
                val sequence = schema.getSequence(objectName)
                    ?: throw RuntimeException(
                        MessageFormat.format(
                            Resources.getString("CannotFindSequence"), name,
                            statement
                        )
                    )
                for (roleName in roles) {
                    var sequencePrivilege = sequence
                        .getPrivilege(roleName)
                    if (sequencePrivilege == null) {
                        sequencePrivilege = PgSequencePrivilege(roleName)
                        sequence.addPrivilege(sequencePrivilege)
                    }
                    for (priv in privileges) {
                        sequencePrivilege.setPrivileges(
                            priv, grant,
                            grantOption
                        )
                    }
                }
            }
        } else if ("SCHEMA".equals(objectType, ignoreCase = true)) {
            if (grant) {
                for (name in identifiers) {
                    // final String sequenceName = parser.parseIdentifier();
                    val schemaName = ParserUtils.getSchemaName(name, database)
                    val schema = database.getSchema(schemaName)
                        ?: throw RuntimeException(
                            MessageFormat.format(
                                Resources.getString("CannotFindSchema"),
                                schemaName, statement
                            )
                        )
                    for (i in privileges.indices) {
                        val privKey = privileges[i]
                        for (roleName in roles) {
                            schema.addGrant("GRANT $privKey ON SCHEMA $name TO $roleName;")
                        }
                    }
                }
            }
        } else {
            if (outputIgnoredStatements) {
                database.addIgnoredStatement(statement)
            }
        }
    }

    private fun parseConsumeFunctionSignature(
        parser: Parser,
        database: PgDatabase, statement: String,
        outputIgnoredStatements: Boolean
    ) {
        parser.expect("(")
        while (!parser.expectOptional(")")) {
            val mode: String?
            mode = if (parser.expectOptional("IN")) {
                "IN"
            } else if (parser.expectOptional("OUT")) {
                "OUT"
            } else if (parser.expectOptional("INOUT")) {
                "INOUT"
            } else if (parser.expectOptional("VARIADIC")) {
                "VARIADIC"
            } else {
                null
            }
            val position = parser.position
            var argumentName: String? = null
            var dataType = parser.parseDataType()
            val position2 = parser.position
            if (!parser.expectOptional(")") && !parser.expectOptional(",")) {
                parser.position = position
                argumentName = ParserUtils.getObjectName(
                    parser
                        .parseIdentifier()
                )
                dataType = parser.parseDataType()
            } else {
                parser.position = position2
            }
            val argument = PgFunction.Argument()
            argument.dataType = dataType
            argument.mode = mode
            argument.name = argumentName
            if (parser.expectOptional(")")) {
                break
            } else {
                parser.expect(",")
            }
        }
    }

    private fun parseColumns(
        parser: Parser,
        database: PgDatabase, statement: String,
        outputIgnoredStatements: Boolean
    ): List<String>? {
        val result: MutableList<String> = ArrayList()
        val present = parser.expectOptional("(")
        if (!present) {
            return null
        }
        var identifier = parser.parseIdentifier()
        result.add(identifier)
        var separator = parser.expectOptionalOneOf(",", ")")
        while (separator != null && ",".equals(separator, ignoreCase = true)) {
            identifier = parser.parseIdentifier()
            result.add(identifier)
            separator = parser.expectOptionalOneOf(",", ")")
        }
        return result
    }
}