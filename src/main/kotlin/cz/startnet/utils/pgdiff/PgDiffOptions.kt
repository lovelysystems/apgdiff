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
    /**
     * Whether Slony triggers should be ignored.
     */
    val isIgnoreSlonyTriggers: Boolean = false,
    /**
     * Whether Schema creation should be ignored.
     */
    val isIgnoreSchemaCreation: Boolean = false,
    val dropCascade: Boolean = false,
)