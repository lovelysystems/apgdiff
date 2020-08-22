/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.loader

import cz.startnet.utils.pgdiff.Resources
import cz.startnet.utils.pgdiff.parsers.*
import cz.startnet.utils.pgdiff.schema.PgDatabase
import java.io.*
import java.text.MessageFormat
import java.util.regex.Pattern

/**
 * Loads PostgreSQL dump into classes.
 *
 * @author fordfrog
 */
object PgDumpLoader {
    //NOPMD
    /**
     * Pattern for testing whether it is CREATE SCHEMA statement.
     */
    private val PATTERN_CREATE_SCHEMA = Pattern.compile(
        "^CREATE[\\s]+SCHEMA[\\s]+.*$",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for parsing default schema (search_path).
     */
    private val PATTERN_DEFAULT_SCHEMA = Pattern.compile(
        "^SET[\\s]+search_path[\\s]*=[\\s]*\"?([^,\\s\"]+)\"?"
                + "(?:,[\\s]+.*)?;$", Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for testing whether it is CREATE TABLE statement.
     */
    private val PATTERN_CREATE_TABLE = Pattern.compile(
        "^CREATE[\\s]+(UNLOGGED\\s|FOREIGN\\s)*TABLE[\\s]+.*$",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for testing whether it is CREATE VIEW or CREATE MATERIALIZED
     * VIEW statement.
     */
    private val PATTERN_CREATE_VIEW = Pattern.compile(
        "^CREATE[\\s]+(?:OR[\\s]+REPLACE[\\s]+)?(?:MATERIALIZED[\\s]+)?VIEW[\\s]+.*$",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for testing whether it is ALTER TABLE statement.
     */
    private val PATTERN_ALTER_TABLE = Pattern.compile(
        "^ALTER[\\s](FOREIGN)*TABLE[\\s]+.*$",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for testing whether it is CREATE SEQUENCE statement.
     */
    private val PATTERN_CREATE_SEQUENCE = Pattern.compile(
        "^CREATE[\\s]+SEQUENCE[\\s]+.*$",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for testing whether it is ALTER SEQUENCE statement.
     */
    private val PATTERN_ALTER_SEQUENCE = Pattern.compile(
        "^ALTER[\\s]+SEQUENCE[\\s]+.*$",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for testing whether it is CREATE INDEX statement.
     */
    private val PATTERN_CREATE_INDEX = Pattern.compile(
        "^CREATE[\\s]+(?:UNIQUE[\\s]+)?INDEX[\\s]+.*$",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for testing whether it is SELECT statement.
     */
    private val PATTERN_SELECT = Pattern.compile(
        "^SELECT[\\s]+.*$", Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for testing whether it is INSERT INTO statement.
     */
    private val PATTERN_INSERT_INTO = Pattern.compile(
        "^INSERT[\\s]+INTO[\\s]+.*$",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for testing whether it is UPDATE statement.
     */
    private val PATTERN_UPDATE = Pattern.compile(
        "^UPDATE[\\s].*$", Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for testing whether it is DELETE FROM statement.
     */
    private val PATTERN_DELETE_FROM = Pattern.compile(
        "^DELETE[\\s]+FROM[\\s]+.*$",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for testing whether it is CREATE TRIGGER statement.
     */
    private val PATTERN_CREATE_TRIGGER = Pattern.compile(
        "^CREATE[\\s]+TRIGGER[\\s]+.*$",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for testing whether it is CREATE FUNCTION or CREATE OR REPLACE
     * FUNCTION statement.
     */
    private val PATTERN_CREATE_FUNCTION = Pattern.compile(
        "^CREATE[\\s]+(?:OR[\\s]+REPLACE[\\s]+)?FUNCTION[\\s]+.*$",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for testing whether it is ALTER VIEW statement.
     */
    private val PATTERN_ALTER_VIEW = Pattern.compile(
        "^ALTER[\\s]+(?:MATERIALIZED[\\s]+)?VIEW[\\s]+.*$",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for testing whether it is COMMENT statement.
     */
    private val PATTERN_COMMENT = Pattern.compile(
        "^COMMENT[\\s]+ON[\\s]+.*$",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for testing whether it is CREATE TYPE statement.
     */
    private val PATTERN_CREATE_TYPE = Pattern.compile(
        "^CREATE[\\s]+TYPE[\\s]+.*$",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for testing whether it is GRANT statement.
     */
    private val PATTERN_GRANT = Pattern.compile(
        "^GRANT[\\s]+.*$", Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for testing whether it is REVOKE statement.
     */
    private val PATTERN_REVOKE = Pattern.compile(
        "^REVOKE[\\s]+.*$", Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for testing a dollar quoting tag.
     */
    private val PATTERN_DOLLAR_TAG = Pattern.compile(
        "[\"\\s]",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for testing whether it is CREATE EXTENSION statement.
     */
    private val PATTERN_CREATE_EXTENSION = Pattern.compile(
        "^CREATE[\\s]+EXTENSION[\\s]+.*$",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for testing whether it is CREATE POLICY statement.
     */
    private val PATTERN_CREATE_POLICY = Pattern.compile(
        "^CREATE[\\s]+POLICY[\\s]+.*$",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for testing whether it is CREATE POLICY statement.
     */
    private val PATTERN_DISABLE_TRIGGER = Pattern.compile(
        "ALTER\\s+TABLE+\\s+\\w+.+\\w+\\s+DISABLE+\\s+TRIGGER+\\s+\\w+.*$",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Pattern for testing whether it is CREATE RULE  statement.
     */
    private val PATTERN_CREATE_RULE = Pattern.compile(
        "^CREATE[\\s]+RULE[\\s]+.*$",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * / **
     * Storage of unprocessed line part.
     */
    private var lineBuffer: String? = null

    /**
     * Loads database schema from dump file.
     *
     * @param inputStream             input stream that should be read
     * @param charsetName             charset that should be used to read the
     * file
     * @param outputIgnoredStatements whether ignored statements should be
     * included in the output
     * @param ignoreSlonyTriggers     whether Slony triggers should be ignored
     * @param ignoreSchemaCreation    whether schema creation should be ignored
     *
     * @return database schema from dump file
     */
    fun loadDatabaseSchema(
        inputStream: InputStream?,
        charsetName: String?, outputIgnoredStatements: Boolean,
        ignoreSlonyTriggers: Boolean, ignoreSchemaCreation: Boolean
    ): PgDatabase {
        val database = PgDatabase()
        val reader = try {
            BufferedReader(
                InputStreamReader(inputStream, charsetName)
            )
        } catch (ex: UnsupportedEncodingException) {
            throw UnsupportedOperationException(
                Resources.getString("UnsupportedEncoding") + ": "
                        + charsetName, ex
            )
        }
        var statement = getWholeStatement(reader)
        while (statement != null) {
            if (PATTERN_CREATE_SCHEMA.matcher(statement).matches()) {
                CreateSchemaParser.parse(database, statement)
            } else if (PATTERN_CREATE_EXTENSION.matcher(statement).matches()) {
                CreateExtensionParser.parse(database, statement)
            } else if (PATTERN_DEFAULT_SCHEMA.matcher(statement).matches()) {
                val matcher = PATTERN_DEFAULT_SCHEMA.matcher(statement)
                matcher.matches()
                database.setDefaultSchema(matcher.group(1))
            } else if (PATTERN_CREATE_TABLE.matcher(statement).matches()) {
                CreateTableParser.parse(database, statement, ignoreSchemaCreation)
            } else if ((PATTERN_ALTER_TABLE.matcher(statement).matches()
                        || PATTERN_ALTER_VIEW.matcher(statement).matches())
                && !PATTERN_DISABLE_TRIGGER.matcher(statement).matches()
            ) {
                AlterRelationParser.parse(
                    database, statement, outputIgnoredStatements
                )
            } else if (PATTERN_CREATE_SEQUENCE.matcher(statement).matches()) {
                CreateSequenceParser.parse(database, statement)
            } else if (PATTERN_ALTER_SEQUENCE.matcher(statement).matches()) {
                AlterSequenceParser.parse(
                    database, statement, outputIgnoredStatements
                )
            } else if (PATTERN_CREATE_INDEX.matcher(statement).matches()) {
                CreateIndexParser.parse(database, statement)
            } else if (PATTERN_CREATE_VIEW.matcher(statement).matches()) {
                CreateViewParser.parse(database, statement)
            } else if (PATTERN_CREATE_TRIGGER.matcher(statement).matches()) {
                CreateTriggerParser.parse(
                    database, statement, ignoreSlonyTriggers
                )
            } else if (PATTERN_DISABLE_TRIGGER.matcher(statement).matches()) {
                CreateTriggerParser.parseDisable(database, statement)
            } else if (PATTERN_CREATE_FUNCTION.matcher(statement).matches()) {
                CreateFunctionParser.parse(database, statement)
            } else if (PATTERN_CREATE_TYPE.matcher(statement).matches()) {
                CreateTypeParser.parse(database, statement)
            } else if (PATTERN_COMMENT.matcher(statement).matches()) {
                CommentParser.parse(
                    database, statement, outputIgnoredStatements
                )
            } else if (PATTERN_SELECT.matcher(statement).matches()
                || PATTERN_INSERT_INTO.matcher(statement).matches()
                || PATTERN_UPDATE.matcher(statement).matches()
                || PATTERN_DELETE_FROM.matcher(statement).matches()
            ) {
            } else if (PATTERN_GRANT.matcher(statement).matches()) {
                GrantRevokeParser.parse(
                    database, statement,
                    outputIgnoredStatements
                )
            } else if (PATTERN_REVOKE.matcher(statement).matches()) {
                GrantRevokeParser.parse(
                    database, statement,
                    outputIgnoredStatements
                )
            } else if (PATTERN_CREATE_POLICY.matcher(statement).matches()) {
                CreatePolicyParser.parse(database, statement)
            } else if (PATTERN_CREATE_RULE.matcher(statement).matches()) {
                CreateRuleParser.parse(database, statement)
            } else if (outputIgnoredStatements) {
                database.addIgnoredStatement(statement)
            } else {
                // these statements are ignored if outputIgnoredStatements
                // is false
            }
            statement = getWholeStatement(reader)
        }
        return database
    }

    /**
     * Loads database schema from dump file.
     *
     * @param file                    name of file containing the dump
     * @param charsetName             charset that should be used to read the
     * file
     * @param outputIgnoredStatements whether ignored statements should be
     * included in the output
     * @param ignoreSlonyTriggers     whether Slony triggers should be ignored
     * @param ignoreSchemaCreation    whether Schema creation should be ignored
     *
     * @return database schema from dump file
     */
    fun loadDatabaseSchema(
        file: String?,
        charsetName: String?, outputIgnoredStatements: Boolean,
        ignoreSlonyTriggers: Boolean, ignoreSchemaCreation: Boolean
    ): PgDatabase {
        if (file == "-") return loadDatabaseSchema(
            System.`in`, charsetName,
            outputIgnoredStatements, ignoreSlonyTriggers, ignoreSchemaCreation
        )
        var fis: FileInputStream? = null
        return try {
            fis = FileInputStream(file)
            loadDatabaseSchema(
                fis, charsetName,
                outputIgnoredStatements, ignoreSlonyTriggers, ignoreSchemaCreation
            )
        } catch (ex: FileNotFoundException) {
            throw FileException(
                MessageFormat.format(
                    Resources.getString("FileNotFound"), file
                ), ex
            )
        } finally {
            if (fis != null) {
                try {
                    fis.close()
                } catch (ex: IOException) {
                }
            }
        }
    }

    /**
     * Reads whole statement from the reader into single-line string.
     *
     * @param reader reader to be read
     *
     * @return whole statement from the reader into single-line string
     */
    private fun getWholeStatement(reader: BufferedReader): String? {
        val sbStatement = StringBuilder(1024)
        if (lineBuffer != null) {
            sbStatement.append(lineBuffer)
            lineBuffer = null
            stripComment(sbStatement)
        }
        var pos = sbStatement.indexOf(";")
        while (true) {
            if (pos == -1) {
                val newLine: String?
                newLine = try {
                    reader.readLine()
                } catch (ex: IOException) {
                    throw FileException(
                        Resources.getString("CannotReadFile"), ex
                    )
                }
                if (newLine == null) {
                    return if (sbStatement.toString().trim { it <= ' ' }.length == 0) {
                        null
                    } else {
                        throw RuntimeException(
                            MessageFormat.format(
                                Resources.getString("EndOfStatementNotFound"),
                                sbStatement.toString()
                            )
                        )
                    }
                }
                if (sbStatement.length > 0) {
                    sbStatement.append(System.getProperty("line.separator"))
                }
                pos = sbStatement.length
                sbStatement.append(newLine)
                stripComment(sbStatement)
                pos = sbStatement.indexOf(";", pos)
            } else {
                if (!isQuoted(sbStatement, pos)) {
                    if (pos == sbStatement.length - 1) {
                        lineBuffer = null
                    } else {
                        lineBuffer = sbStatement.substring(pos + 1)
                        sbStatement.setLength(pos + 1)
                    }
                    return sbStatement.toString().trim { it <= ' ' }
                }
                pos = sbStatement.indexOf(";", pos + 1)
            }
        }
    }

    /**
     * Strips comment from statement line.
     *
     * @param sbStatement string builder containing statement
     */
    private fun stripComment(sbStatement: StringBuilder) {
        var pos = sbStatement.indexOf("--")
        while (pos >= 0) {
            if (pos == 0) {
                sbStatement.setLength(0)
                return
            } else {
                if (!isQuoted(sbStatement, pos)) {
                    sbStatement.setLength(pos)
                    return
                }
            }
            pos = sbStatement.indexOf("--", pos + 1)
        }
        var endPos = sbStatement.indexOf("*/")
        while (endPos >= 0) {
            if (!isQuoted(sbStatement, endPos)) {
                val startPos = sbStatement.lastIndexOf("/*", endPos)
                if (startPos < endPos && !isQuoted(sbStatement, startPos)) {
                    sbStatement.replace(startPos, endPos + 2, "")
                }
            }
            endPos = sbStatement.indexOf("*/", endPos + 2)
        }
    }

    /**
     * Checks whether specified position in the string builder is quoted. It
     * might be quoted either by single quote or by dollar sign quoting.
     *
     * @param sbString string builder
     * @param pos      position to be checked
     *
     * @return true if the specified position is quoted, otherwise false
     */
    private fun isQuoted(
        sbString: StringBuilder,
        pos: Int
    ): Boolean {
        var isQuoted = false
        var insideDoubleQuotes = false
        var insideSingeQuote = false // Determine if double quote is inside of a single quote.
        var curPos = 0
        while (curPos < pos) {

            // Check if the quote is inside of a double quotes
            if (sbString[curPos] == '\"' && !insideSingeQuote) {
                insideDoubleQuotes = !insideDoubleQuotes
            }
            if (sbString[curPos] == '\'' && !insideDoubleQuotes) {
                insideSingeQuote = !insideSingeQuote
            }
            if (!insideDoubleQuotes) {
                if (sbString[curPos] == '\'') {
                    isQuoted = !isQuoted

                    // if quote was escaped by backslash, it's like double quote
                    if (pos > 0 && sbString[pos - 1] == '\\') {
                        isQuoted = !isQuoted
                    }
                } else if (sbString[curPos] == '$' && !isQuoted) {
                    val endPos = sbString.indexOf("$", curPos + 1)
                    if (endPos == -1) {
                        return false
                    }
                    val tag = sbString.substring(curPos, endPos + 1)
                    if (!isCorrectTag(tag)) {
                        return false
                    }
                    val endTagPos = sbString.indexOf(tag, endPos + 1)

                    // if end tag was not found or it was found after the checked
                    // position, it's quoted
                    if (endTagPos == -1 || endTagPos > pos) {
                        return true
                    }
                    curPos = endTagPos + tag.length - 1
                }
            }
            curPos++
        }
        return isQuoted
    }

    /**
     * Checks whether dollar quoting tag is correct.
     *
     * @param tag tag to be checked
     *
     * @return true if the tag is correct, otherwise false
     */
    private fun isCorrectTag(tag: String): Boolean {
        return !PATTERN_DOLLAR_TAG.matcher(tag).find()
    }
}