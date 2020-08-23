package cz.startnet.utils.pgdiff

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.PrintWriter
import java.util.*

class PgDiffTest {

    init {
        Locale.setDefault(Locale.ENGLISH)
    }

    fun parameters(): List<Args> {
        return listOf(
            Args("modify_column_type"),
            Args("add_cluster"),
            Args("drop_cluster"),
            Args("modify_cluster"),
            Args("add_extension"),
            Args("drop_extension"),
            Args("drop_with_oids"),
            Args("add_index"),
            Args("add_index_only"),
            Args("drop_index"),
            Args("drop_index_with_cluster"),
            Args("modify_index"),
            Args("add_statistics"),
            Args("modify_statistics"),
            Args("drop_statistics"),
            Args("add_default_value"),
            Args("modify_default_value"),
            Args("drop_default_value"),
            Args("add_not_null"),
            Args("drop_not_null"),
            Args("add_column"),
            Args("drop_column"),
            Args("add_table"),
            Args("add_table_partition"),
            Args("drop_table"),
            Args("add_constraint"),
            Args("modify_constraint"),
            Args("drop_constraint"),
            Args("add_unique_constraint", false, false, false, true),
            Args("read_inherits"),
            Args("add_inherits"),
            Args("modify_inherits"),
            Args("alter_inherited_column"),
            Args("add_inherits_default_column"),
            Args("add_inherits_schema_default_column"),
            Args("modify_default_value_inherited_column"),
            Args("add_sequence"),
            Args("drop_sequence"),
            Args("modify_sequence_increment"),
            Args("modify_sequence_start_ignore_off"),
            Args("modify_sequence_start_ignore_on", false, false, false, true),
            Args("modify_sequence_minvalue_set"),
            Args("modify_sequence_minvalue_unset"),
            Args("modify_sequence_maxvalue_set"),
            Args("modify_sequence_maxvalue_unset"),
            Args("modify_sequence_cache"),
            Args("modify_sequence_cycle_on"),
            Args("modify_sequence_cycle_off"),
            Args("modify_function_end_detection"),
            Args("add_function_noargs"),
            Args("drop_function_noargs"),
            Args("modify_function_noargs"),
            Args("add_function_args"),
            Args("drop_function_args"),
            Args("modify_function_args"),
            Args("add_function_args2"),
            Args("drop_function_args2"),
            Args("modify_function_args2"),
            Args("add_function_similar"),
            Args("drop_function_similar"),
            Args("modify_function_similar"),
            Args("function_equal_whitespace", false, false, true, false),
            Args("add_trigger"),
            Args("drop_trigger"),
            Args("modify_trigger"),
            Args("add_view"),
            Args("drop_view"),
            Args("modify_view"),
            Args("add_materialized_view"),
            Args("drop_materialized_view"),
            Args("modify_materialized_view"),
            Args("add_defaults", true, false, false, false),
            Args("multiple_schemas"),
            Args("multiple_schemas", false, true, false, false),
            Args("alter_view_drop_default", false, true, false, false),
            Args("alter_view_add_default", false, true, false, false),
            Args("add_comments", false, true, false, false),
            Args("drop_comments", false, true, false, false),
            Args("alter_comments", false, true, false, false),
            Args("alter_view_change_default", false, true, false, false),
            Args("add_sequence_bug2100013", false, true, false, false),
            Args("add_sequence_issue225"),
            Args("view_bug3080388", false, true, false, false),
            Args("function_bug3084274", false, true, false, false),
            Args("add_comment_new_column", false, true, false, false),
            Args("quoted_schema", false, true, false, false),
            Args("add_column_add_defaults", true, true, false, false),
            Args("add_owned_sequence", false, true, false, false),
            Args("add_empty_table"),
            Args("view_colnames"),
            Args("add_table_bug102"),
            Args("add_unlogged_table"),
            Args("drop_unlogged_table"),
            Args("add_table_issue115"),
            Args("add_column_issue134"),
            Args("add_column_issue188"),
            Args("add_column_issue188"),
            Args("view_alias_with_quote"),
            Args("view_triggers"),
            Args("grant_on_table_sequence"),
            Args("revoke_on_table_sequence"),
            Args("grant_on_view"),
            Args("revoke_on_view"),
            Args("grant_on_columns"),
            Args("add_type"),
            Args("drop_type"),
            Args("alter_type"),
            Args("foreign_create_table"),
            Args("foreign_drop_table"),
            Args("foreign_alter_type"),
            Args("enable_force_rls"),
            Args("disable_no_force_rls"),
            Args("create_policies"),
            Args("drop_policies"),
            Args("alter_policies"),
            Args("disable_trigger"),
            Args("grant_on_new_sequence"),
            Args("alter_view_owner"),
            Args("grant_on_table_cols_mixed"),
            Args("grant_on_view_cols_mixed")

        );
    }

    /**
     * Runs single test on original schema.
     *
     * expected diff.
     */
    @ParameterizedTest
    @MethodSource("parameters")
    fun runDiffSameOriginal(args: Args) {
        val diffInput = ByteArrayOutputStream()
        val writer = PrintWriter(diffInput, true)
        val arguments = PgDiffArguments()
        PgDiff.createDiff(
            writer, arguments,
            testFileAsStream(args.fileNameTemplate + "_original.sql"),
            testFileAsStream(args.fileNameTemplate + "_original.sql")
        )

        writer.flush()
        diffInput.toString().trim { it <= ' ' }.shouldBeEmpty()
    }

    /**
     * Runs single test on new schema.
     *
     * expected diff.
     */
    @ParameterizedTest
    @MethodSource("parameters")
    fun runDiffSameNew(args: Args) {
        val diffInput = ByteArrayOutputStream()
        val writer = PrintWriter(diffInput, true)
        val arguments = PgDiffArguments()
        PgDiff.createDiff(
            writer, arguments,
            testFileAsStream(args.fileNameTemplate + "_new.sql"),
            testFileAsStream(args.fileNameTemplate + "_new.sql")
        )
        writer.flush()
        diffInput.toString().trim { it <= ' ' }.shouldBeEmpty()
    }

    //
    /**
     * Runs single test using class member variables.
     *
     * expected diff.
     */
    @ParameterizedTest
    @MethodSource("parameters")
    fun runDiff(args: Args) {
        val diffInput = ByteArrayOutputStream()
        val writer = PrintWriter(diffInput, true)
        val arguments = PgDiffArguments()
        arguments.isAddDefaults = args.addDefaults
        arguments.isIgnoreFunctionWhitespace = args.ignoreFunctionWhitespace
        arguments.isIgnoreStartWith = args.ignoreStartWith
        PgDiffUtils.setUseExists(true)
        PgDiff.createDiff(
            writer, arguments,
            testFileAsStream(args.fileNameTemplate + "_original.sql"),
            testFileAsStream(args.fileNameTemplate + "_new.sql")
        )
        writer.flush()
        val reader = testFileAsStream(args.fileNameTemplate + "_diff.sql").bufferedReader()
        val part = CharArray(1024)
        val sbExpDiff = StringBuilder(1024)
        while (reader.read(part) != -1) {
            sbExpDiff.append(part)
        }
        reader.close()

        sbExpDiff.toString().trim { it <= ' ' } shouldBe diffInput.toString().trim { it <= ' ' }
    }

    private fun testFileAsStream(path: String): InputStream {
        return javaClass.getResourceAsStream("/pgdiff_test_files/" + path) ?: error("sql file not found $path")
    }
}

data class Args(
    /**
     * Template name for file names that should be used for the test. Testing
     * method adds _original.sql, _new.sql and _diff.sql to the file name
     * template.
     */
    val fileNameTemplate: String,
    /**
     * Value for the same named command line argument.
     */
    val addDefaults: Boolean = false,
    /**
     * Value for the same named command line argument.
     */
    val addTransaction: Boolean = false,
    /**
     * Value for the same named command line argument.
     */
    val ignoreFunctionWhitespace: Boolean = false,
    /**
     * Value for the same named command line argument.
     */
    val ignoreStartWith: Boolean = false

) {
    override fun toString(): String {
        return fileNameTemplate
    }
}
