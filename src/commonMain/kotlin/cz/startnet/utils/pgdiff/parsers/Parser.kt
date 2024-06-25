/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.parsers

fun interface SubParser {

    operator fun invoke(parser: Parser, ctx: ParserContext): Boolean
}

class ParserContextException(val parser: Parser, cause: Exception) : RuntimeException(parser.string, cause)

/**
 * Class for parsing strings.
 *
 * @author fordfrog
 */
class Parser(val string: String, val statementNum: Int = 0) {

    /**
     * Current position.
     */
    var position = 0

    fun <R> withErrorContext(block: () -> R): R {
        try {
            return block()
        } catch (e: Exception) {
            throw ParserContextException(this, e)
        }
    }

    /**
     * Checks whether the string contains given word on current position. If not
     * then throws an exception.
     *
     * @param words list of words to check
     */
    fun expect(vararg words: String) {
        for (word in words) {
            expect(word, false)
        }
    }

    /**
     * Checks whether the string contains given word on current position. If not
     * and expectation is optional then position is not changed and method
     * returns true. If expectation is not optional, exception with error
     * description is thrown. If word is found, position is moved at first
     * non-whitespace character following the word.
     *
     * @param word     word to expect
     * @param optional true if word is optional, otherwise false
     *
     * @return true if word was found, otherwise false
     */
    fun expect(word: String, optional: Boolean): Boolean {
        val wordEnd = position + word.length
        if (wordEnd <= string.length && string.substring(position, wordEnd).equals(word, ignoreCase = true)
            && (wordEnd == string.length || string[wordEnd].isWhitespace()
                    || string[wordEnd] == '(' || string[wordEnd] == ')' || string[wordEnd] == ';' || string[wordEnd] == ',' || string[wordEnd] == '[' || "(" == word || "," == word || "[" == word || "]" == word)
        ) {
            position = wordEnd
            skipWhitespace()
            return true
        }
        if (optional) {
            return false
        }
        var dumpEndPosition = position + 20
        if (string.length - (position + 1) < 20) {
            dumpEndPosition = string.length - 1
        }
        throw parseError(word, position + 1, this.string.substring(position, dumpEndPosition))
    }

    private fun parseError(expected: String, startPos: Int, contextString: String = "") =
        ParserException(
            "Cannot parse string: $string\nExpected $expected at position $startPos ''$contextString''"
        )


    /**
     * Checks whether string contains at current position sequence of the words.
     *
     * @param words array of words
     *
     * @return true if whole sequence was found, otherwise false
     */
    fun expectOptional(vararg words: String): Boolean {
        val oldPosition = position
        var found = expect(words[0], true)
        if (!found) {
            return false
        }
        for (i in 1 until words.size) {
            skipWhitespace()
            found = expect(words[i], true)
            if (!found) {
                position = oldPosition
                return false
            }
        }
        return true
    }

    /**
     * Moves position in the string to next non-whitespace string.
     */
    fun skipWhitespace() {
        while (position < string.length) {
            if (!string[position].isWhitespace()) {
                break
            }
            position++
        }
    }

    /**
     * Parses identifier from current position. If identifier is quoted, it is
     * returned quoted. If the identifier is not quoted, it is converted to
     * lowercase. If identifier does not start with letter then exception is
     * thrown. Position is placed at next first non-whitespace character.
     *
     * @return parsed identifier
     */
    fun parseIdentifier(): String {
        var identifier = parseIdentifierInternal()
        while (string[position] == '.') {
            position++
            identifier += '.'.toString() + parseIdentifierInternal()
        }
        if (string[position] == '.') {
            position++
            identifier += '.'.toString() + parseIdentifierInternal()
        }
        skipWhitespace()
        return identifier
    }

    /**
     * Parses single part of the identifier.
     *
     * @return parsed identifier
     */
    private fun parseIdentifierInternal(): String {
        val quoted = string[position] == '"'
        return if (quoted) {
            val endPos = string.indexOf('"', position + 1)
            val result = string.substring(position, endPos + 1)
            position = endPos + 1
            result
        } else {
            var endPos = position
            while (endPos < string.length) {
                val chr = string[endPos]
                if (chr.isWhitespace() || chr == ',' || chr == ')' || chr == '(' || chr == ';' || chr == '.') {
                    break
                }
                endPos++
            }
            val result = string.substring(position, endPos).lowercase()
            position = endPos
            result
        }
    }

    /**
     * Returns rest of the string. If the string ends with ';' then it is
     * removed from the string before returned. If there is nothing more in the
     * string, null is returned.
     *
     * @return rest of the string, without trailing ';' if present, or null if
     * there is nothing more in the string
     */
    val rest: String?
        get() {
            val result: String = if (string[string.length - 1] == ';') {
                if (position == string.length - 1) {
                    return null
                } else {
                    string.substring(position, string.length - 1)
                }
            } else {
                string.substring(position)
            }
            position = string.length
            return result
        }

    /**
     * Parses integer from the string. If next word is not integer then
     * exception is thrown.
     *
     * @return parsed integer value
     */
    fun parseInteger(): Int {
        var endPos = position
        while (endPos < string.length) {
            if (!string[endPos].isLetterOrDigit()) {
                break
            }
            endPos++
        }
        return try {
            val result = string.substring(position, endPos).toInt()
            position = endPos
            skipWhitespace()
            result
        } catch (ex: NumberFormatException) {
            throw parseError(
                "integer", position + 1, string.substring(position, position + 20)
            )
        }
    }

    /**
     * Parses string from the string. String can be either quoted or unqouted.
     * Quoted string is parsed till next unescaped quote. Unquoted string is
     * parsed till whitespace, ',' ')' or ';' is found. If string should be
     * empty, exception is thrown.
     *
     * @return parsed string, if quoted then including quotes
     */
    fun parseString(): String {
        val quoted = string[position] == '\''
        return if (quoted) {
            var escape = false
            var endPos = position + 1
            while (endPos < string.length) {
                val chr = string[endPos]
                if (chr == '\\') {
                    escape = !escape
                } else if (!escape && chr == '\'') {
                    if (endPos + 1 < string.length
                        && string[endPos + 1] == '\''
                    ) {
                        endPos++
                    } else {
                        break
                    }
                }
                endPos++
            }
            val result: String
            try {
                if (endPos >= string.length) {
                    //try to fix StringIndexOutOfBoundsException
                    endPos = string.lastIndexOf('\'')
                }
                result = string.substring(position, endPos + 1)
            } catch (ex: Throwable) {
                throw RuntimeException(
                    "Failed to get substring: " + string
                            + " start pos: " + position + " end pos: "
                            + (endPos + 1), ex
                )
            }
            position = endPos + 1
            skipWhitespace()
            result
        } else {
            var endPos = position
            while (endPos < string.length) {
                val chr = string[endPos]
                if (chr.isWhitespace() || chr == ',' || chr == ')' || chr == ';') {
                    break
                }
                endPos++
            }
            if (position == endPos) {
                throw parseError("string", position + 1)
            }
            val result = string.substring(position, endPos)
            position = endPos
            skipWhitespace()
            result
        }
    }

    /**
     * Returns expression that is ended either with ',', ')' or with end of the
     * string. If expression is empty then exception is thrown.
     *
     * @return expression string
     */
    val expression: String
        get() {
            val endPos = expressionEnd
            if (position == endPos) {
                parseError(
                    "expression", position + 1, string.substring(position, position + 20)
                )
            }
            val result = string.substring(position, endPos).trim { it <= ' ' }
            position = endPos
            return result
        }// escaped single quote is like two single quotes

    /**
     * Returns position of last character of single command within statement
     * (like CREATE TABLE). Last character is either ',' or ')'. If no such
     * character is found and method reaches the end of the command then
     * position after the last character in the command is returned.
     *
     * @return end position of the command
     *
     * @todo Support for dollar quoted strings is missing here.
     */
    private val expressionEnd: Int
        get() {
            var bracesCount = 0
            var singleQuoteOn = false
            var charPos = position
            while (charPos < string.length) {
                val chr = string[charPos]
                if (chr == '(' || chr == '[') {
                    bracesCount++
                } else if (chr == ')' || chr == ']') {
                    if (bracesCount == 0) {
                        break
                    } else {
                        bracesCount--
                    }
                } else if (chr == '\'') {
                    singleQuoteOn = !singleQuoteOn

                    // escaped single quote is like two single quotes
                    if (charPos > 0 && string[charPos - 1] == '\\') {
                        singleQuoteOn = !singleQuoteOn
                    }
                } else if (chr == ',' && !singleQuoteOn && bracesCount == 0) {
                    break
                } else if (chr == ';' && bracesCount == 0 && !singleQuoteOn) {
                    break
                }
                charPos++
            }
            return charPos
        }

    /**
     * Throws exception about unsupported command in statement.
     */
    fun throwUnsupportedCommand() {
        val ctxSTring = string.substring(position, if (string.length > position + 20) position + 20 else string.length)
        throw ParserException(
            """Cannot parse string: $string\nUnsupported command at position ${position + 1} ''$ctxSTring''"""
        )
    }

    /**
     * Checks whether one of the words is present at current position. If the
     * word is present then the word is returned and position is updated.
     *
     * @param words words to check
     *
     * @return found word or null if non of the words has been found
     *
     * @see .expectOptional
     */
    fun expectOptionalOneOf(vararg words: String?): String? {
        for (word in words) {
            if (expectOptional(word!!)) {
                return word
            }
        }
        return null
    }

    /**
     * Returns substring from the string.
     *
     * @param startPos start position
     * @param endPos   end position exclusive
     *
     * @return substring
     */
    fun getSubString(startPos: Int, endPos: Int): String {
        return string.substring(startPos, endPos)
    }

    /**
     * Parses data type from the string. Position is updated. If data type
     * definition is not found then exception is thrown.
     *
     * @return data type string
     */
    fun parseDataType(): String {
        var endPos = position
        while (endPos < string.length && !string[endPos].isWhitespace()
            && string[endPos] != '(' && string[endPos] != ')' && string[endPos] != ','
        ) {
            endPos++
        }
        if (endPos == position) {
            parseError(
                "data type definition", position + 1, string.substring(position, position + 20)
            )
        }
        var dataType = string.substring(position, endPos)
        position = endPos
        skipWhitespace()
        if ("character".equals(dataType, ignoreCase = true)
            && expectOptional("varying")
        ) {
            dataType = "character varying"
        } else if ("double".equals(dataType, ignoreCase = true)
            && expectOptional("precision")
        ) {
            dataType = "double precision"
        }
        val timestamp = ("timestamp".equals(dataType, ignoreCase = true)
                || "time".equals(dataType, ignoreCase = true))
        if (string[position] == '(') {
            dataType += expression
        }
        if (timestamp) {
            if (expectOptional("with", "time", "zone")) {
                dataType += " with time zone"
            } else if (expectOptional("without", "time", "zone")) {
                dataType += " without time zone"
            }
        }
        if (expectOptional("[")) {
            expect("]")
            dataType += "[]"
        }
        return dataType
    }

    /**
     * Checks whether the whole string has been consumed.
     *
     * @return true if there is nothing left to parse, otherwise false
     */
    val isConsumed: Boolean
        get() = (position == string.length
                || position + 1 == string.length
                && string[position] == ';')

    init {
        skipWhitespace()
    }
}
