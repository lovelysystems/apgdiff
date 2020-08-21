/**
 * Copyright 2010 StartNet s.r.o.
 */
package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.schema.PgDatabase
import cz.startnet.utils.pgdiff.schema.PgSchema
import org.hamcrest.core.IsEqual
import org.junit.Assert
import org.junit.Test

/**
 * Tests [.ParserUtils].
 *
 * @author fordfrog
 */
class ParserUtilsTest {
    @Test(timeout = 1000)
    fun testParseSchemaBothQuoted() {
        val database = PgDatabase()
        val schema = PgSchema("juzz_system")
        database.addSchema(schema)
        Assert.assertThat(
            ParserUtils.getSchemaName(
                "\"juzz_system\".\"f_obj_execute_node_select\"", database
            ),
            IsEqual.equalTo("juzz_system")
        )
    }

    @Test(timeout = 1000)
    fun testParseSchemaFirstQuoted() {
        val database = PgDatabase()
        val schema = PgSchema("juzz_system")
        database.addSchema(schema)
        Assert.assertThat(
            ParserUtils.getSchemaName(
                "\"juzz_system\".f_obj_execute_node_select", database
            ),
            IsEqual.equalTo("juzz_system")
        )
    }

    @Test(timeout = 1000)
    fun testParseSchemaSecondQuoted() {
        val database = PgDatabase()
        val schema = PgSchema("juzz_system")
        database.addSchema(schema)
        Assert.assertThat(
            ParserUtils.getSchemaName(
                "juzz_system.\"f_obj_execute_node_select\"", database
            ),
            IsEqual.equalTo("juzz_system")
        )
    }

    @Test(timeout = 1000)
    fun testParseSchemaNoneQuoted() {
        val database = PgDatabase()
        val schema = PgSchema("juzz_system")
        database.addSchema(schema)
        Assert.assertThat(
            ParserUtils.getSchemaName(
                "juzz_system.f_obj_execute_node_select", database
            ),
            IsEqual.equalTo("juzz_system")
        )
    }

    @Test(timeout = 1000)
    fun testParseSchemaThreeQuoted() {
        val database = PgDatabase()
        val schema = PgSchema("juzz_system")
        database.addSchema(schema)
        Assert.assertThat(
            ParserUtils.getSchemaName(
                "\"juzz_system\".\"f_obj_execute_node_select\".\"test\"",
                database
            ), IsEqual.equalTo("juzz_system")
        )
    }

    @Test(timeout = 1000)
    fun testParseObjectBothQuoted() {
        Assert.assertThat(
            ParserUtils.getObjectName(
                "\"juzz_system\".\"f_obj_execute_node_select\""
            ),
            IsEqual.equalTo("f_obj_execute_node_select")
        )
    }

    @Test(timeout = 1000)
    fun testParseObjectFirstQuoted() {
        Assert.assertThat(
            ParserUtils.getObjectName(
                "\"juzz_system\".f_obj_execute_node_select"
            ),
            IsEqual.equalTo("f_obj_execute_node_select")
        )
    }

    @Test(timeout = 1000)
    fun testParseObjectSecondQuoted() {
        Assert.assertThat(
            ParserUtils.getObjectName(
                "juzz_system.\"f_obj_execute_node_select\""
            ),
            IsEqual.equalTo("f_obj_execute_node_select")
        )
    }

    @Test(timeout = 1000)
    fun testParseObjectNoneQuoted() {
        Assert.assertThat(
            ParserUtils.getObjectName(
                "juzz_system.f_obj_execute_node_select"
            ),
            IsEqual.equalTo("f_obj_execute_node_select")
        )
    }

    @Test(timeout = 1000)
    fun testParseObjectThreeQuoted() {
        Assert.assertThat(
            ParserUtils.getObjectName(
                "\"juzz_system\".\"f_obj_execute_node_select\".\"test\""
            ),
            IsEqual.equalTo("test")
        )
    }
}