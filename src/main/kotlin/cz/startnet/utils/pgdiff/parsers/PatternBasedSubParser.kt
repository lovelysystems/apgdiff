package cz.startnet.utils.pgdiff.parsers

import java.util.regex.Pattern

open class PatternBasedSubParser(val pattern: Pattern) : SubParser {

    constructor(vararg regexes: String) : this(
        regexes.joinToString(")|(", "(", ")")
    )

    constructor(regex: String) : this(
        Pattern.compile(regex, Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
    )

    open fun parse(parser: Parser, ctx: ParserContext) {}

    override fun invoke(parser: Parser, ctx: ParserContext): Boolean {
        if (pattern.matcher(parser.string).matches()) {
            parse(parser, ctx)
            return true
        }
        return false
    }
}
