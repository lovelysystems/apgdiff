package cz.startnet.utils.pgdiff.parsers

import cz.startnet.utils.pgdiff.schema.PgDatabase
import cz.startnet.utils.pgdiff.schema.PgSchema
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ParserUtilsTest {
    @Test
    fun testParseSchemaBothQuoted() {
        val database = PgDatabase()
        val schema = PgSchema("juzz_system")
        database.addSchema(schema)
        ParserUtils.getSchemaName(
            "\"juzz_system\".\"f_obj_execute_node_select\"", database
        ) shouldBe "juzz_system"
    }

    @Test
    fun testParseSchemaFirstQuoted() {
        val database = PgDatabase()
        val schema = PgSchema("juzz_system")
        database.addSchema(schema)
        ParserUtils.getSchemaName(
            "\"juzz_system\".f_obj_execute_node_select", database
        ) shouldBe "juzz_system"
    }

    @Test
    fun testParseSchemaSecondQuoted() {
        val database = PgDatabase()
        val schema = PgSchema("juzz_system")
        database.addSchema(schema)
        ParserUtils.getSchemaName(
            "juzz_system.\"f_obj_execute_node_select\"", database
        ) shouldBe "juzz_system"
    }

    @Test
    fun testParseSchemaNoneQuoted() {
        val database = PgDatabase()
        val schema = PgSchema("juzz_system")
        database.addSchema(schema)
        ParserUtils.getSchemaName(
            "juzz_system.f_obj_execute_node_select", database
        ) shouldBe "juzz_system"
    }

    @Test
    fun testParseSchemaThreeQuoted() {
        val database = PgDatabase()
        val schema = PgSchema("juzz_system")
        database.addSchema(schema)
        ParserUtils.getSchemaName(
            "\"juzz_system\".\"f_obj_execute_node_select\".\"test\"",
            database
        ) shouldBe "juzz_system"
    }

    @Test
    fun testParseObjectBothQuoted() {
        ParserUtils.getObjectName(
            "\"juzz_system\".\"f_obj_execute_node_select\""
        ) shouldBe "f_obj_execute_node_select"
    }

    @Test
    fun testParseObjectFirstQuoted() {
        ParserUtils.getObjectName(
            "\"juzz_system\".f_obj_execute_node_select"
        ) shouldBe "f_obj_execute_node_select"
    }

    @Test
    fun testParseObjectSecondQuoted() {
        ParserUtils.getObjectName(
            "juzz_system.\"f_obj_execute_node_select\""
        ) shouldBe "f_obj_execute_node_select"
    }

    @Test
    fun testParseObjectNoneQuoted() {
        ParserUtils.getObjectName(
            "juzz_system.f_obj_execute_node_select"
        ) shouldBe "f_obj_execute_node_select"
    }

    @Test
    fun testParseObjectThreeQuoted() {
        ParserUtils.getObjectName(
            "\"juzz_system\".\"f_obj_execute_node_select\".\"test\""
        ) shouldBe "test"
    }
}