package cz.startnet.utils.pgdiff.parsers

expect fun createRegex(pattern: String): Regex

open class PatternBasedSubParser(val pattern: Regex) : SubParser {

    constructor(vararg regexes: String) : this(
        regexes.joinToString(")|(", "(", ")")
    )

    constructor(regex: String) : this(createRegex(regex))

    open fun parse(parser: Parser, ctx: ParserContext) {}

    override fun invoke(parser: Parser, ctx: ParserContext): Boolean {
        if (pattern.matches(parser.string)) {
            parse(parser, ctx)
            return true
        }
        return false
    }
}
