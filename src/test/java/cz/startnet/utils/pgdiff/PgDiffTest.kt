/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.*
import java.util.*

/**
 * Tests for PgDiff class.
 *
 * @author fordfrog
 */
@RunWith(value = Parameterized::class)
class PgDiffTest(
    /**
     * Template name for file names that should be used for the test. Testing
     * method adds _original.sql, _new.sql and _diff.sql to the file name
     * template.
     */
    private val fileNameTemplate: String,
    /**
     * Value for the same named command line argument.
     */
    private val addDefaults: Boolean,
    /**
     * Value for the same named command line argument.
     */
    private val addTransaction: Boolean,
    /**
     * Value for the same named command line argument.
     */
    private val ignoreFunctionWhitespace: Boolean,
    /**
     * Value for the same named command line argument.
     */
    private val ignoreStartWith: Boolean
) {
    /**
     * Runs single test on original schema.
     *
     * @throws FileNotFoundException Thrown if expected diff file was not found.
     * @throws IOException           Thrown if problem occurred while reading
     * expected diff.
     */
    @Test
    fun runDiffSameOriginal() {
        val diffInput = ByteArrayOutputStream()
        val writer = PrintWriter(diffInput, true)
        val arguments = PgDiffArguments()
        PgDiff.createDiff(
            writer, arguments,
            PgDiffTest::class.java.getResourceAsStream(
                fileNameTemplate + "_original.sql"
            ),
            PgDiffTest::class.java.getResourceAsStream(
                fileNameTemplate + "_original.sql"
            )
        )
        writer.flush()
        Assert.assertEquals("File name template: $fileNameTemplate",
            "", diffInput.toString().trim { it <= ' ' })
    }

    /**
     * Runs single test on new schema.
     *
     * @throws FileNotFoundException Thrown if expected diff file was not found.
     * @throws IOException           Thrown if problem occurred while reading
     * expected diff.
     */
    @Test
    fun runDiffSameNew() {
        val diffInput = ByteArrayOutputStream()
        val writer = PrintWriter(diffInput, true)
        val arguments = PgDiffArguments()
        PgDiff.createDiff(
            writer, arguments,
            PgDiffTest::class.java.getResourceAsStream(
                fileNameTemplate + "_new.sql"
            ),
            PgDiffTest::class.java.getResourceAsStream(
                fileNameTemplate + "_new.sql"
            )
        )
        writer.flush()
        Assert.assertEquals("File name template: $fileNameTemplate",
            "", diffInput.toString().trim { it <= ' ' })
    }

    /**
     * Runs single test using class member variables.
     *
     * @throws FileNotFoundException Thrown if expected diff file was not found.
     * @throws IOException           Thrown if problem occurred while reading
     * expected diff.
     */
    @Test
    fun runDiff() {
        val diffInput = ByteArrayOutputStream()
        val writer = PrintWriter(diffInput, true)
        val arguments = PgDiffArguments()
        arguments.isAddDefaults = addDefaults
        arguments.isIgnoreFunctionWhitespace = ignoreFunctionWhitespace
        arguments.isIgnoreStartWith = ignoreStartWith
        PgDiffUtils.setUseExists(true)
        PgDiff.createDiff(
            writer, arguments,
            PgDiffTest::class.java.getResourceAsStream(
                fileNameTemplate + "_original.sql"
            ),
            PgDiffTest::class.java.getResourceAsStream(
                fileNameTemplate + "_new.sql"
            )
        )
        writer.flush()
        val reader = BufferedReader(
            InputStreamReader(
                PgDiffTest::class.java.getResourceAsStream(
                    fileNameTemplate + "_diff.sql"
                )
            )
        )
        val part = CharArray(1024)
        val sbExpDiff = StringBuilder(1024)
        while (reader.read(part) != -1) {
            sbExpDiff.append(part)
        }
        reader.close()
        Assert.assertEquals("File name template: $fileNameTemplate",
            sbExpDiff.toString().trim { it <= ' ' },
            diffInput.toString().trim { it <= ' ' })
    }

    companion object {
        /**
         * Provides parameters for running the tests.
         *
         * @return parameters for the tests
         */
        @JvmStatic
        @Parameterized.Parameters
        fun parameters(): Collection<*> {
            return Arrays.asList(
                *arrayOf(
                    arrayOf<Any>("modify_column_type", false, false, false, false),
                    arrayOf<Any>("add_cluster", false, false, false, false),
                    arrayOf<Any>("drop_cluster", false, false, false, false),
                    arrayOf<Any>("modify_cluster", false, false, false, false),
                    arrayOf<Any>("add_extension", false, false, false, false),
                    arrayOf<Any>("drop_extension", false, false, false, false),
                    arrayOf<Any>("drop_with_oids", false, false, false, false),
                    arrayOf<Any>("add_index", false, false, false, false),
                    arrayOf<Any>("add_index_only", false, false, false, false),
                    arrayOf<Any>("drop_index", false, false, false, false),
                    arrayOf<Any>("drop_index_with_cluster", false, false, false, false),
                    arrayOf<Any>("modify_index", false, false, false, false),
                    arrayOf<Any>("add_statistics", false, false, false, false),
                    arrayOf<Any>("modify_statistics", false, false, false, false),
                    arrayOf<Any>("drop_statistics", false, false, false, false),
                    arrayOf<Any>("add_default_value", false, false, false, false),
                    arrayOf<Any>("modify_default_value", false, false, false, false),
                    arrayOf<Any>("drop_default_value", false, false, false, false),
                    arrayOf<Any>("add_not_null", false, false, false, false),
                    arrayOf<Any>("drop_not_null", false, false, false, false),
                    arrayOf<Any>("add_column", false, false, false, false),
                    arrayOf<Any>("drop_column", false, false, false, false),
                    arrayOf<Any>("add_table", false, false, false, false),
                    arrayOf<Any>("add_table_partition", false, false, false, false),
                    arrayOf<Any>("drop_table", false, false, false, false),
                    arrayOf<Any>("add_constraint", false, false, false, false),
                    arrayOf<Any>("modify_constraint", false, false, false, false),
                    arrayOf<Any>("drop_constraint", false, false, false, false),
                    arrayOf<Any>("add_unique_constraint", false, false, false, true),
                    arrayOf<Any>("read_inherits", false, false, false, false),
                    arrayOf<Any>("add_inherits", false, false, false, false),
                    arrayOf<Any>("modify_inherits", false, false, false, false),
                    arrayOf<Any>("alter_inherited_column", false, false, false, false),
                    arrayOf<Any>("add_inherits_default_column", false, false, false, false),
                    arrayOf<Any>("add_inherits_schema_default_column", false, false, false, false),
                    arrayOf<Any>("modify_default_value_inherited_column", false, false, false, false),
                    arrayOf<Any>("add_sequence", false, false, false, false),
                    arrayOf<Any>("drop_sequence", false, false, false, false),
                    arrayOf<Any>("modify_sequence_increment", false, false, false, false),
                    arrayOf<Any>(
                        "modify_sequence_start_ignore_off", false, false, false,
                        false
                    ),
                    arrayOf<Any>(
                        "modify_sequence_start_ignore_on", false, false, false,
                        true
                    ),
                    arrayOf<Any>("modify_sequence_minvalue_set", false, false, false, false),
                    arrayOf<Any>(
                        "modify_sequence_minvalue_unset", false, false, false,
                        false
                    ),
                    arrayOf<Any>("modify_sequence_maxvalue_set", false, false, false, false),
                    arrayOf<Any>(
                        "modify_sequence_maxvalue_unset", false, false, false,
                        false
                    ),
                    arrayOf<Any>("modify_sequence_cache", false, false, false, false),
                    arrayOf<Any>("modify_sequence_cycle_on", false, false, false, false),
                    arrayOf<Any>("modify_sequence_cycle_off", false, false, false, false),
                    arrayOf<Any>("modify_function_end_detection", false, false, false, false),
                    arrayOf<Any>("add_function_noargs", false, false, false, false),
                    arrayOf<Any>("drop_function_noargs", false, false, false, false),
                    arrayOf<Any>("modify_function_noargs", false, false, false, false),
                    arrayOf<Any>("add_function_args", false, false, false, false),
                    arrayOf<Any>("drop_function_args", false, false, false, false),
                    arrayOf<Any>("modify_function_args", false, false, false, false),
                    arrayOf<Any>("add_function_args2", false, false, false, false),
                    arrayOf<Any>("drop_function_args2", false, false, false, false),
                    arrayOf<Any>("modify_function_args2", false, false, false, false),
                    arrayOf<Any>("add_function_similar", false, false, false, false),
                    arrayOf<Any>("drop_function_similar", false, false, false, false),
                    arrayOf<Any>("modify_function_similar", false, false, false, false),
                    arrayOf<Any>("function_equal_whitespace", false, false, true, false),
                    arrayOf<Any>("add_trigger", false, false, false, false),
                    arrayOf<Any>("drop_trigger", false, false, false, false),
                    arrayOf<Any>("modify_trigger", false, false, false, false),
                    arrayOf<Any>("add_view", false, false, false, false),
                    arrayOf<Any>("drop_view", false, false, false, false),
                    arrayOf<Any>("modify_view", false, false, false, false),
                    arrayOf<Any>("add_materialized_view", false, false, false, false),
                    arrayOf<Any>("drop_materialized_view", false, false, false, false),
                    arrayOf<Any>("modify_materialized_view", false, false, false, false),
                    arrayOf<Any>("add_defaults", true, false, false, false),
                    arrayOf<Any>("multiple_schemas", false, false, false, false),
                    arrayOf<Any>("multiple_schemas", false, true, false, false),
                    arrayOf<Any>("alter_view_drop_default", false, true, false, false),
                    arrayOf<Any>("alter_view_add_default", false, true, false, false),
                    arrayOf<Any>("add_comments", false, true, false, false),
                    arrayOf<Any>("drop_comments", false, true, false, false),
                    arrayOf<Any>("alter_comments", false, true, false, false),
                    arrayOf<Any>("alter_view_change_default", false, true, false, false),
                    arrayOf<Any>("add_sequence_bug2100013", false, true, false, false),
                    arrayOf<Any>("add_sequence_issue225", false, false, false, false),
                    arrayOf<Any>("view_bug3080388", false, true, false, false),
                    arrayOf<Any>("function_bug3084274", false, true, false, false),
                    arrayOf<Any>("add_comment_new_column", false, true, false, false),
                    arrayOf<Any>("quoted_schema", false, true, false, false),
                    arrayOf<Any>("add_column_add_defaults", true, true, false, false),
                    arrayOf<Any>("add_owned_sequence", false, true, false, false),
                    arrayOf<Any>("add_empty_table", false, false, false, false),
                    arrayOf<Any>("view_colnames", false, false, false, false),
                    arrayOf<Any>("add_table_bug102", false, false, false, false),
                    arrayOf<Any>("add_unlogged_table", false, false, false, false),
                    arrayOf<Any>("drop_unlogged_table", false, false, false, false),
                    arrayOf<Any>("add_table_issue115", false, false, false, false),
                    arrayOf<Any>("add_column_issue134", false, false, false, false),
                    arrayOf<Any>("add_column_issue188", false, false, false, false),
                    arrayOf<Any>("add_column_issue188", false, false, false, false),
                    arrayOf<Any>("view_alias_with_quote", false, false, false, false),
                    arrayOf<Any>("view_triggers", false, false, false, false),
                    arrayOf<Any>("grant_on_table_sequence", false, false, false, false),
                    arrayOf<Any>("revoke_on_table_sequence", false, false, false, false),
                    arrayOf<Any>("grant_on_view", false, false, false, false),
                    arrayOf<Any>("revoke_on_view", false, false, false, false),
                    arrayOf<Any>("grant_on_columns", false, false, false, false),
                    arrayOf<Any>("add_type", false, false, false, false),
                    arrayOf<Any>("drop_type", false, false, false, false),
                    arrayOf<Any>("alter_type", false, false, false, false),
                    arrayOf<Any>("foreign_create_table", false, false, false, false),
                    arrayOf<Any>("foreign_drop_table", false, false, false, false),
                    arrayOf<Any>("foreign_alter_type", false, false, false, false),
                    arrayOf<Any>("enable_force_rls", false, false, false, false),
                    arrayOf<Any>("disable_no_force_rls", false, false, false, false),
                    arrayOf<Any>("create_policies", false, false, false, false),
                    arrayOf<Any>("drop_policies", false, false, false, false),
                    arrayOf<Any>("alter_policies", false, false, false, false),
                    arrayOf<Any>("disable_trigger", false, false, false, false),
                    arrayOf<Any>("grant_on_new_sequence", false, false, false, false),
                    arrayOf<Any>("alter_view_owner", false, false, false, false),
                    arrayOf<Any>("grant_on_table_cols_mixed", false, false, false, false),
                    arrayOf<Any>("grant_on_view_cols_mixed", false, false, false, false)
                )
            )
        }
    }

    /**
     * Creates a new PgDiffTest object.
     *
     * @param fileNameTemplate         [.fileNameTemplate]
     * @param addDefaults              [.addDefaults]
     * @param addTransaction           [.addTransaction]
     * @param ignoreFunctionWhitespace [.ignoreFunctionWhitespace]
     * @param ignoreStartWith          [.ignoreStartWith]
     */
    init {
        Locale.setDefault(Locale.ENGLISH)
    }
}