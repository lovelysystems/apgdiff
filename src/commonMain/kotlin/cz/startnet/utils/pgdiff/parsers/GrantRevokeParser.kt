package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.schema.PgColumnPrivilege
import cz.startnet.utils.pgdiff.schema.PgRelationPrivilege
import cz.startnet.utils.pgdiff.schema.PgSequencePrivilege

/**
 * Parses GRANT statements.
 *
 * @author user
 */
object GrantRevokeParser : PatternBasedSubParser(
    "^GRANT[\\s]+.*$",
    "^REVOKE[\\s]+.*$"
) {
    override fun parse(parser: Parser, ctx: ParserContext) {
        val grant: Boolean
        val privileges: MutableList<String?> = ArrayList()
        val privilegesColumns: MutableList<List<String>?> = ArrayList()
        val identifiers: MutableList<String> = ArrayList()
        val roles: MutableList<String?> = ArrayList()
        var grantOption = false
        val revokeMode: String?
        val parser = Parser(parser.string)
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
        var columns: List<String>?
        if (privilege == null) {
            // unknown privilege so unsupported object privilege
            // object role_name is using a different syntax so will always pass
            // here
            ctx.database.addIgnoredStatement(parser.string)
            return
        }
        if ("ALL".equals(privilege, ignoreCase = true)) {
            parser.expectOptional("PRIVILEGES")
        }
        columns = if ("ALL".equals(privilege, ignoreCase = true) || "SELECT".equals(
                privilege,
                ignoreCase = true
            ) || "INSERT".equals(privilege, ignoreCase = true) || "UPDATE".equals(
                privilege,
                ignoreCase = true
            ) || "REFERENCES".equals(privilege, ignoreCase = true)
        ) {
            parseColumns(
                parser
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
                        parser
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
            ctx.database.addIgnoredStatement(parser.string)
            return
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
        var identifier = parser.parseIdentifier()
        if ("FUNCTION".equals(objectType, ignoreCase = true)
            || "ALL FUNCTIONS IN SCHEMA".equals(objectType, ignoreCase = true)
        ) {
            parseConsumeFunctionSignature(
                parser
            )
        }
        identifiers.add(identifier)
        while (parser.expectOptional(",")) {
            identifier = parser.parseIdentifier()
            if ("FUNCTION".equals(objectType, ignoreCase = true)
                || "ALL FUNCTIONS IN SCHEMA"
                    .equals(objectType, ignoreCase = true)
            ) {
                parseConsumeFunctionSignature(
                    parser
                )
            }
            identifiers.add(identifier)
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
                ctx.database.addIgnoredStatement(parser.string)
                return
            }
        }
        if ("TABLE".equals(objectType, ignoreCase = true)) {
            for (name in identifiers) {
                val schemaName = ctx.database.getSchemaName(name)
                val schema = parser.withErrorContext {
                    ctx.database.getSchemaSafe(schemaName)
                }
                val objectName = ParserUtils.getObjectName(name)
                val rel = schema.getView(objectName) ?: schema.getTableSafe(objectName)
                for (i in privileges.indices) {
                    val privKey = privileges[i]
                    val privValue = privilegesColumns[i]
                    if (privValue != null) {
                        for (columnName in privValue) {
                            val column = rel.getColumnSafe(columnName)
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
                val schemaName = ctx.database.getSchemaName(name)
                val schema = parser.withErrorContext {
                    ctx.database.getSchemaSafe(schemaName)
                }
                val objectName = ParserUtils.getObjectName(name)
                val sequence = schema.getSequenceSafe(objectName)
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
                    val schemaName = ctx.database.getSchemaName(name)
                    val schema = parser.withErrorContext {
                        ctx.database.getSchemaSafe(schemaName)
                    }
                    for (i in privileges.indices) {
                        val privKey = privileges[i]
                        for (roleName in roles) {
                            schema.addGrant("GRANT $privKey ON SCHEMA $name TO $roleName;")
                        }
                    }
                }
            }
        } else {
            ctx.database.addIgnoredStatement(parser.string)
        }
    }

    private fun parseConsumeFunctionSignature(
        parser: Parser
    ) {
        parser.parseFunctionArguments()
    }

    private fun parseColumns(
        parser: Parser
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