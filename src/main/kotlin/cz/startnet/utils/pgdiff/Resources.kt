/**
 * Copyright 2010 StartNet s.r.o.
 */
package cz.startnet.utils.pgdiff

private val RESOURCE_BUNDLE = mapOf(
    "OriginalDatabaseIgnoredStatements" to "Original database ignored statements",
    "NewDatabaseIgnoredStatements" to "New database ignored statements",
    "ErrorUnknownOption" to "ERROR: Unknown option",
    "WarningUnableToDetermineStorageType" to "WARNING: Column {0} in new table has no STORAGE set but in old table storage was set. Unable to determine STORAGE type.",
    "TypeParameterChange" to "TYPE change - table: {0} original: {1} new: {2}",
    "UnsupportedEncoding" to "Unsupported encoding",
    "CannotReadFile" to "Cannot read file",
    "FileNotFound" to "File ''{0}'' not found",
    "CannotFindColumnInTable" to "Cannot find column ''{0}'' in table ''{1}''",
    "CannotParseStringExpectedWord" to "Cannot parse string: {0}\nExpected {1} at position {2} ''{3}''",
    "CannotParseStringExpectedInteger" to "Cannot parse string: {0}\nExpected integer at position {1} ''{2}''",
    "CannotParseStringExpectedString" to "Cannot parse string: {0}\nExpected string at position {1}",
    "CannotParseStringExpectedExpression" to "Cannot parse string: {0}\nExpected expression at position {1} ''{2}''",
    "CannotParseStringUnsupportedCommand" to "Cannot parse string: {0}\nUnsupported command at position {1} ''{2}''",
    "CannotParseStringExpectedDataType" to "Cannot parse string: {0}\nExpected data type definition at position {1} ''{2}''",
    "CannotFindSchema" to "Cannot find schema ''{0}'' for statement ''{1}''. Missing CREATE SCHEMA statement?",
    "CannotFindView" to "Cannot find view ''{0}'' for statement ''{1}''. Missing CREATE VIEW statement?",
    "CannotFindObject" to "Cannot find object ''{0}'' for statement ''{1}''.",
    "CannotFindTableColumn" to "Cannot find column ''{0}'' in table ''{1}'' for statement ''{2}''.",
    "CannotFindTable" to "Cannot find table ''{0}'' for statement ''{1}''. Missing CREATE TABLE?",
    "CannotFindSequence" to "Cannot find sequence ''{0}'' for statement ''{1}''. Missing CREATE SEQUENCE?",
    "EndOfStatementNotFound" to "Cannot find ending semicolon of statement: {0}",
    "CreateTablePrimaryKeyNotSupported" to """CREATE TABLE ... PRIMARY KEY ..." is not supported. Use "CREATE TABLE ... CONSTRAINT name PRIMARY KEY ..." instead.""",
    "CreateTableUniqueNotSupported" to """CREATE TABLE ... UNIQUE ..." is not supported. Use "CREATE TABLE ... CONSTRAINT name UNIQUE..." instead."""
)


object Resources {
    /**
     * Returns string from resource bundle based on the key.
     *
     * @param key key
     *
     * @return string
     */
    fun getString(key: String): String {
        return requireNotNull(RESOURCE_BUNDLE[key])
    }
}