package cz.startnet.utils.pgdiff.parsers

actual fun createRegex(pattern: String): Regex {
    return Regex(pattern, setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
}