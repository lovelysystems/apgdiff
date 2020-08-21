/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.schema

/**
 * Stores relPrivileges information.
 *
 * @author user
 */
class PgRelationPrivilege
/**
 * Creates a new PgTablePrivilege object.
 *
 * @param roleName
 * name of the role
 */(
    /**
     * @return the roleName
     */
    val roleName: String?
) {
    private var select = false
    private var insert = false
    private var update = false
    private var delete = false
    private var truncate = false
    private var references = false
    private var trigger = false
    private var selectWithGrantOption = false
    private var insertWithGrantOption = false
    private var updateWithGrantOption = false
    private var deleteWithGrantOption = false
    private var truncateWithGrantOption = false
    private var referencesWithGrantOption = false
    private var triggerWithGrantOption = false
    fun setPrivileges(
        privilege: String?, value: Boolean,
        grantOption: Boolean
    ) {
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
        if ("INSERT".equals(privilege, ignoreCase = true)
            || "ALL".equals(privilege, ignoreCase = true)
        ) {
            if (value) {
                insert = true
                if (grantOption) {
                    insertWithGrantOption = true
                }
            } else {
                insertWithGrantOption = false
                if (!grantOption) {
                    insert = false
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
        if ("DELETE".equals(privilege, ignoreCase = true)
            || "ALL".equals(privilege, ignoreCase = true)
        ) {
            if (value) {
                delete = true
                if (grantOption) {
                    deleteWithGrantOption = true
                }
            } else {
                deleteWithGrantOption = false
                if (!grantOption) {
                    delete = false
                }
            }
        }
        if ("TRUNCATE".equals(privilege, ignoreCase = true)
            || "ALL".equals(privilege, ignoreCase = true)
        ) {
            if (value) {
                truncate = true
                if (grantOption) {
                    truncateWithGrantOption = true
                }
            } else {
                truncateWithGrantOption = false
                if (!grantOption) {
                    truncate = false
                }
            }
        }
        if ("REFERENCES".equals(privilege, ignoreCase = true)
            || "ALL".equals(privilege, ignoreCase = true)
        ) {
            if (value) {
                references = true
                if (grantOption) {
                    referencesWithGrantOption = true
                }
            } else {
                referencesWithGrantOption = false
                if (!grantOption) {
                    references = false
                }
            }
        }
        if ("TRIGGER".equals(privilege, ignoreCase = true)
            || "ALL".equals(privilege, ignoreCase = true)
        ) {
            if (value) {
                trigger = true
                if (grantOption) {
                    triggerWithGrantOption = true
                }
            } else {
                triggerWithGrantOption = false
                if (!grantOption) {
                    trigger = false
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
    fun isSimilar(other: PgRelationPrivilege?): Boolean {
        if (other == null) {
            return false
        }
        if (select != other.select) {
            return false
        }
        if (selectWithGrantOption != other.selectWithGrantOption) {
            return false
        }
        if (insert != other.insert) {
            return false
        }
        if (insertWithGrantOption != other.insertWithGrantOption) {
            return false
        }
        if (update != other.update) {
            return false
        }
        if (updateWithGrantOption != other.updateWithGrantOption) {
            return false
        }
        if (delete != other.delete) {
            return false
        }
        if (deleteWithGrantOption != other.deleteWithGrantOption) {
            return false
        }
        if (truncate != other.truncate) {
            return false
        }
        if (truncateWithGrantOption != other.truncateWithGrantOption) {
            return false
        }
        if (references != other.references) {
            return false
        }
        if (referencesWithGrantOption != other.referencesWithGrantOption) {
            return false
        }
        if (trigger != other.trigger) {
            return false
        }
        return if (triggerWithGrantOption != other.triggerWithGrantOption) {
            false
        } else true
    }

    fun getPrivilegesSQL(withGrantOption: Boolean): String {
        return if (withGrantOption) {
            if (selectWithGrantOption && insertWithGrantOption
                && updateWithGrantOption && deleteWithGrantOption
                && truncateWithGrantOption && referencesWithGrantOption
                && triggerWithGrantOption
            ) {
                return "ALL"
            }
            var result = ""
            if (selectWithGrantOption) {
                if ("" != result) {
                    result += ", "
                }
                result += "SELECT"
            }
            if (insertWithGrantOption) {
                if ("" != result) {
                    result += ", "
                }
                result += "INSERT"
            }
            if (updateWithGrantOption) {
                if ("" != result) {
                    result += ", "
                }
                result += "UPDATE"
            }
            if (deleteWithGrantOption) {
                if ("" != result) {
                    result += ", "
                }
                result += "DELETE"
            }
            if (truncateWithGrantOption) {
                if ("" != result) {
                    result += ", "
                }
                result += "TRUNCATE"
            }
            if (referencesWithGrantOption) {
                if ("" != result) {
                    result += ", "
                }
                result += "REFERENCES"
            }
            if (triggerWithGrantOption) {
                if ("" != result) {
                    result += ", "
                }
                result += "TRIGGER"
            }
            result
        } else {
            if (select && insert && update && delete && truncate && references
                && trigger
            ) {
                return "ALL"
            }
            var result = ""
            if (select) {
                if ("" != result) {
                    result += ", "
                }
                result += "SELECT"
            }
            if (insert) {
                if ("" != result) {
                    result += ", "
                }
                result += "INSERT"
            }
            if (update) {
                if ("" != result) {
                    result += ", "
                }
                result += "UPDATE"
            }
            if (delete) {
                if ("" != result) {
                    result += ", "
                }
                result += "DELETE"
            }
            if (truncate) {
                if ("" != result) {
                    result += ", "
                }
                result += "TRUNCATE"
            }
            if (references) {
                if ("" != result) {
                    result += ", "
                }
                result += "REFERENCES"
            }
            if (trigger) {
                if ("" != result) {
                    result += ", "
                }
                result += "TRIGGER"
            }
            result
        }
    }
}