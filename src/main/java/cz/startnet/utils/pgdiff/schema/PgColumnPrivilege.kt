/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.schema

/**
 * Stores tablePrivileges information.
 *
 * @author user
 */
class PgColumnPrivilege
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
    private var references = false
    private var selectWithGrantOption = false
    private var insertWithGrantOption = false
    private var updateWithGrantOption = false
    private var referencesWithGrantOption = false
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
    }

    /**
     * true the privileges are the same (no matter of roleName).
     *
     * @param other
     * privileges to compare
     * @return isSimilar
     */
    fun isSimilar(other: PgColumnPrivilege?): Boolean {
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
        if (references != other.references) {
            return false
        }
        return if (referencesWithGrantOption != other.referencesWithGrantOption) {
            false
        } else true
    }

    fun getPrivilegesSQL(
        withGrantOption: Boolean,
        columnName: String?
    ): String {
        return if (withGrantOption) {
            if (selectWithGrantOption && insertWithGrantOption
                && updateWithGrantOption && referencesWithGrantOption
            ) {
                return "ALL ($columnName)"
            }
            var result = ""
            if (selectWithGrantOption) {
                if ("" != result) {
                    result += ", "
                }
                result += "SELECT ($columnName)"
            }
            if (insertWithGrantOption) {
                if ("" != result) {
                    result += ", "
                }
                result += "INSERT ($columnName)"
            }
            if (updateWithGrantOption) {
                if ("" != result) {
                    result += ", "
                }
                result += "UPDATE ($columnName)"
            }
            if (referencesWithGrantOption) {
                if ("" != result) {
                    result += ", "
                }
                result += "REFERENCES ($columnName)"
            }
            result
        } else {
            if (select && insert && update && references) {
                return "ALL ($columnName)"
            }
            var result = ""
            if (select) {
                if ("" != result) {
                    result += ", "
                }
                result += "SELECT ($columnName)"
            }
            if (insert) {
                if ("" != result) {
                    result += ", "
                }
                result += "INSERT ($columnName)"
            }
            if (update) {
                if ("" != result) {
                    result += ", "
                }
                result += "UPDATE ($columnName)"
            }
            if (references) {
                if ("" != result) {
                    result += ", "
                }
                result += "REFERENCES ($columnName)"
            }
            result
        }
    }
}