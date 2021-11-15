package cz.startnet.utils.pgdiff.loader

import cz.startnet.utils.pgdiff.Resources
import cz.startnet.utils.pgdiff.parsers.*
import cz.startnet.utils.pgdiff.schema.PgDatabase
import java.io.BufferedReader
import java.io.IOException
import java.text.MessageFormat
import java.util.regex.Pattern

object PgDumpLoader {

    /**
     * Pattern for testing a dollar quoting tag.
     */
    private val PATTERN_DOLLAR_TAG = Pattern.compile(
        "[\"\\s]",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )

    /**
     * Storage of unprocessed line part.
     */
    private var lineBuffer: String? = null

    fun loadDatabaseSchema(
        reader: BufferedReader
    ): PgDatabase {
        val database = PgDatabase()
        val ctx = ParserContext(
            database
        )
        val subParsers = listOf<SubParser>(
            IgnoredStatementParser("^(CREATE|ALTER)[\\s]+OPERATOR[\\s]+(FAMILY|CLASS)[\\s]+.*$"),
            CreateSchemaParser,
            CreateDomainParser,
            CreateOperatorParser,
            AlterDomainParser,
            AlterSchemaParser,
            CreateExtensionParser,
            DefaultSchemaParser,
            CreateTableParser,
            DisableTriggerParser,
            AlterRelationParser,
            CreateSequenceParser,
            AlterSequenceParser,
            CreateIndexParser,
            CreateViewParser,
            CreateTriggerParser,
            CreateFunctionParser,
            AlterFunctionParser,
            AlterOperatorParser,
            CreateTypeParser,
            AlterTypeParser,
            CommentParser,
            // ignore DML
            PatternBasedSubParser(
                "^SELECT[\\s]+.*$",
                "^INSERT[\\s]+INTO[\\s]+.*$",
                "^UPDATE[\\s].*$",
                "^DELETE[\\s]+FROM[\\s]+.*$",
            ),
            GrantRevokeParser,
            CreatePolicyParser,
            CreateRuleParser
        )

        var statement = getWholeStatement(reader)
        while (statement != null) {
            val parser = Parser(statement)
            val matchedSubParser = subParsers.firstOrNull {
                it(parser, ctx)
            }
            if (matchedSubParser == null) {
                database.addIgnoredStatement(statement)
            }
            statement = getWholeStatement(reader)
        }
        return database
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