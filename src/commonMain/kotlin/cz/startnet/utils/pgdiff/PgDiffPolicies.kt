/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.schema.PgPolicy
import cz.startnet.utils.pgdiff.schema.PgSchema
import kotlin.text.StringBuilder

object PgDiffPolicies {
    fun createPolicies(
        writer: StringBuilder,
        oldSchema: PgSchema?, newSchema: PgSchema?
    ) {
        for (newTable in newSchema?.tables.orEmpty()) {
            val newTableName = newTable.name
            val oldTable = oldSchema?.getTable(newTableName)
            for (policy in newTable.policies) {
                val oldPolicy = oldTable?.getPolicy(policy.name)
                if (oldPolicy == null) {

                    createPolicySQL(writer, policy)
                }
            }
        }
    }

    fun alterPolicies(
        writer: StringBuilder,
        oldSchema: PgSchema?, newSchema: PgSchema?
    ) {
        for (newTable in newSchema?.tables.orEmpty()) {
            val newTableName = newTable.name
            if (oldSchema != null) {
                val oldTable = oldSchema.getTable(newTableName)
                if (oldTable != null) {
                    for (policy in oldTable.policies) {
                        val newPolicy = newTable.getPolicy(policy.name)
                        if (newPolicy != null) {
                            // ALTER POLICY doesn't support changing command(ALL,
                            // SELECT..) so we drop it and create it
                            val newCommand = newPolicy.command
                            val oldCommand = policy.command
                            if (newCommand != null && oldCommand != null && newCommand != oldCommand) {

                                dropPolicySQL(writer, newPolicy)
                                createPolicySQL(writer, newPolicy)
                            } else if (policy.using == null && newPolicy.using != null
                                || policy.using != null && newPolicy.using == null
                                || policy.using != null && newPolicy.using != null && policy.using != newPolicy.using
                            ) {

                                alterPolicySQL(writer, newPolicy)
                            } else if (policy.withCheck == null && newPolicy.withCheck != null
                                || policy.withCheck != null && newPolicy.withCheck == null
                                || policy.withCheck != null && newPolicy.withCheck != null && policy.withCheck != newPolicy.withCheck
                            ) {

                                alterPolicySQL(writer, newPolicy)
                            } else {
                                val equalRoles = newPolicy.roles.containsAll(policy.roles) &&
                                        policy.roles.containsAll(newPolicy.roles)
                                if (!equalRoles) {

                                    alterPolicySQL(writer, newPolicy)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun dropPolicies(
        writer: StringBuilder,
        oldSchema: PgSchema?, newSchema: PgSchema?
    ) {
        for (newTable in newSchema?.tables.orEmpty()) {
            val newTableName = newTable.name
            if (oldSchema != null) {
                val oldTable = oldSchema.getTable(newTableName)
                if (oldTable != null) {
                    for (policy in oldTable.policies) {
                        if (newTable.getPolicy(policy.name) == null) {
                            dropPolicySQL(writer, policy)
                        }
                    }
                }
            }
        }
    }

    private fun createPolicySQL(writer: StringBuilder, policy: PgPolicy) {
        writer.append(
            "CREATE POLICY "
                    + PgDiffUtils.getQuotedName(policy.name)
                    + " ON "
                    + PgDiffUtils.getQuotedName(policy.tableName!!)
        )
        writer.append(" FOR " + policy.command)
        var roles = ""
        writer.append(" TO ")
        val iterator: Iterator<String?> = policy.roles.iterator()
        while (iterator.hasNext()) {
            roles += iterator.next().toString() + if (iterator.hasNext()) ", " else ""
        }
        writer.append(roles)
        if (policy.using != null) {
            writer.println()
            writer.appendLine("USING (")
            writer.append("  ")
            writer.println(policy.using)
            writer.append(")")
        }
        if (policy.withCheck != null) {
            writer.println()
            writer.appendLine("WITH CHECK (")
            writer.append("  ")
            writer.println(policy.withCheck)
            writer.append(")")
        }
        writer.appendLine(";")
    }

    private fun alterPolicySQL(writer: StringBuilder, policy: PgPolicy) {
        writer.append(
            "ALTER POLICY "
                    + PgDiffUtils.getQuotedName(policy.name)
                    + " ON "
                    + PgDiffUtils.getQuotedName(policy.tableName!!)
        )
        var roles = ""
        writer.append(" TO ")
        val iterator: Iterator<String?> = policy.roles.iterator()
        while (iterator.hasNext()) {
            roles += iterator.next().toString() + if (iterator.hasNext()) ", " else ""
        }
        writer.append(roles)
        if (policy.using != null) {
            writer.println()
            writer.appendLine("USING (")
            writer.append("  ")
            writer.println(policy.using)
            writer.append(")")
        }
        if (policy.withCheck != null) {
            writer.println()
            writer.appendLine("WITH CHECK (")
            writer.append("  ")
            writer.println(policy.withCheck)
            writer.append(")")
        }
        writer.appendLine(";")
    }

    private fun dropPolicySQL(writer: StringBuilder, policy: PgPolicy) {
        writer.appendLine(
            "DROP POLICY "
                    + PgDiffUtils.getQuotedName(policy.name)
                    + " ON "
                    + PgDiffUtils.getQuotedName(policy.tableName!!)
                    + ";"
        )
    }
}