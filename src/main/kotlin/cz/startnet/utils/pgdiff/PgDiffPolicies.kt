/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff

import cz.startnet.utils.pgdiff.schema.PgPolicy
import cz.startnet.utils.pgdiff.schema.PgSchema
import java.io.PrintWriter

object PgDiffPolicies {
    fun createPolicies(
        writer: PrintWriter,
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
        writer: PrintWriter,
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
        writer: PrintWriter,
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

    private fun createPolicySQL(writer: PrintWriter, policy: PgPolicy) {
        writer.print(
            "CREATE POLICY "
                    + PgDiffUtils.getQuotedName(policy.name)
                    + " ON "
                    + PgDiffUtils.getQuotedName(policy.tableName!!)
        )
        writer.print(" FOR " + policy.command)
        var roles = ""
        writer.print(" TO ")
        val iterator: Iterator<String?> = policy.roles.iterator()
        while (iterator.hasNext()) {
            roles += iterator.next().toString() + if (iterator.hasNext()) ", " else ""
        }
        writer.print(roles)
        if (policy.using != null) {
            writer.println()
            writer.println("USING (")
            writer.print("  ")
            writer.println(policy.using)
            writer.print(")")
        }
        if (policy.withCheck != null) {
            writer.println()
            writer.println("WITH CHECK (")
            writer.print("  ")
            writer.println(policy.withCheck)
            writer.print(")")
        }
        writer.println(";")
    }

    private fun alterPolicySQL(writer: PrintWriter, policy: PgPolicy) {
        writer.print(
            "ALTER POLICY "
                    + PgDiffUtils.getQuotedName(policy.name)
                    + " ON "
                    + PgDiffUtils.getQuotedName(policy.tableName!!)
        )
        var roles = ""
        writer.print(" TO ")
        val iterator: Iterator<String?> = policy.roles.iterator()
        while (iterator.hasNext()) {
            roles += iterator.next().toString() + if (iterator.hasNext()) ", " else ""
        }
        writer.print(roles)
        if (policy.using != null) {
            writer.println()
            writer.println("USING (")
            writer.print("  ")
            writer.println(policy.using)
            writer.print(")")
        }
        if (policy.withCheck != null) {
            writer.println()
            writer.println("WITH CHECK (")
            writer.print("  ")
            writer.println(policy.withCheck)
            writer.print(")")
        }
        writer.println(";")
    }

    private fun dropPolicySQL(writer: PrintWriter, policy: PgPolicy) {
        writer.println(
            "DROP POLICY "
                    + PgDiffUtils.getQuotedName(policy.name)
                    + " ON "
                    + PgDiffUtils.getQuotedName(policy.tableName!!)
                    + ";"
        )
    }
}