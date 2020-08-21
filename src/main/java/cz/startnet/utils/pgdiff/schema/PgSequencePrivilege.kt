/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.schema

/**
 * Stores sequencePrivileges information.
 *
 * @author user
 */
class PgSequencePrivilege
/**
 * Creates a new PgSequencePrivilege object.
 *
 * @param roleName
 * name of the role
 */(
    /**
     * @return the roleName
     */
    val roleName: String?
) {
    private var usage = false
    private var select = false
    private var update = false
    private var usageWithGrantOption = false
    private var selectWithGrantOption = false
    private var updateWithGrantOption = false
    fun setPrivileges(
        privilege: String?, value: Boolean,
        grantOption: Boolean
    ) {
        if ("USAGE".equals(privilege, ignoreCase = true)
            || "ALL".equals(privilege, ignoreCase = true)
        ) {
            if (value) {
                usage = true
                if (grantOption) {
                    usageWithGrantOption = true
                }
            } else {
                usageWithGrantOption = false
                if (!grantOption) {
                    usage = false
                }
            }
        }
        if ("SELECT".equals(privilege, ignoreCase = true)
            || "ALL".equals(privilege, ignoreCase = true)
        ) {
            if (value) {
                select = true
                if (grantOption) {
                    selectWithGrantOption = true
                }
            } else {
                selectWithGrantOption = false
                if (!grantOption) {
                    select = false
                }
            }
        }
        if ("UPDATE".equals(privilege, ignoreCase = true)
            || "ALL".equals(privilege, ignoreCase = true)
        ) {
            if (value) {
                update = true
                if (grantOption) {
                    updateWithGrantOption = true
                }
            } else {
                updateWithGrantOption = false
                if (!grantOption) {
                    update = false
                }
            }
        }
    }

    /**
     * true the privileges are the same (no matter of roleName).
     *
     * @param other
     * privileges to compare
     * @return isSimilar
     */
    fun isSimilar(other: PgSequencePrivilege?): Boolean {
        if (other == null) {
            return false
        }
        if (usage != other.usage) {
            return false
        }
        if (usageWithGrantOption != other.usageWithGrantOption) {
            return false
        }
        if (select != other.select) {
            return false
        }
        if (selectWithGrantOption != other.selectWithGrantOption) {
            return false
        }
        if (update != other.update) {
            return false
        }
        return if (updateWithGrantOption != other.updateWithGrantOption) {
            false
        } else true
    }

    fun getPrivilegesSQL(withGrantOption: Boolean): String {
        return if (withGrantOption) {
            if (usageWithGrantOption && selectWithGrantOption
                && updateWithGrantOption
            ) {
                return "ALL"
            }
            var result = ""
            if (usageWithGrantOption) {
                if ("" != result) {
                    result += ", "
                }
                result += "USAGE"
            }
            if (selectWithGrantOption) {
                if ("" != result) {
                    result += ", "
                }
                result += "SELECT"
            }
            if (updateWithGrantOption) {
                if ("" != result) {
                    result += ", "
                }
                result += "UPDATE"
            }
            result
        } else {
            if (usage && select && update) {
                return "ALL"
            }
            var result = ""
            if (select) {
                if ("" != result) {
                    result += ", "
                }
                result += "SELECT"
            }
            if (usage) {
                if ("" != result) {
                    result += ", "
                }
                result += "USAGE"
            }
            if (update) {
                if ("" != result) {
                    result += ", "
                }
                result += "UPDATE"
            }
            result
        }
    }
}