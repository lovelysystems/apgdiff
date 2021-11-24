package cz.startnet.utils.pgdiff

data class PgDiffOptions(
    /**
     * Output file charset name.
     */
    val outCharsetName: String = "UTF-8",
    /**
     * Whether DEFAULT ... should be added in case new column has NOT NULL
     * constraint. The default value is dropped later.
     */
    val isAddDefaults: Boolean = false,
    /**
     * Whether to enclose all statements in transaction.
     */
    val isAddTransaction: Boolean = false,
    /**
     * Whether to ignore whitespace while comparing content of functions.
     */
    val isIgnoreFunctionWhitespace: Boolean = false,

    val dropCascade: Boolean = false,

    val schemas: List<String> = emptyList(),

    val excludeSchemas: List<String> = emptyList(),

    val outputIgnoredStatements: Boolean = false
) {

    private val schemaInclPatterns = schemas.map(::Regex)
    private val schemaExclPatterns = excludeSchemas.map(::Regex)

    fun schemaIncluded(name: String): Boolean {
        val included = (schemaInclPatterns.isEmpty() || (schemaInclPatterns.firstOrNull {
            it.matchEntire(name) != null
        } != null))
        val excluded = (schemaExclPatterns.isNotEmpty() && (schemaExclPatterns.firstOrNull {
            it.matchEntire(name) != null
        } != null))
        return (included && !excluded)
    }

}