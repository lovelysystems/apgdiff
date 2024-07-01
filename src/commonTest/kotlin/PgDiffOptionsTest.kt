package cz.startnet.utils.pgdiff

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.test.Test

class PgDiffOptionsTest {
    @Test
    fun `empty options should accept any schema`() {
        val options = PgDiffOptions()
        options.schemaIncluded("blah").shouldBeTrue()
        options.schemaIncluded("somethingOdd ").shouldBeTrue()
    }

    @Test
    fun `a single schema inclusion excludes all others`() {
        val options = PgDiffOptions(schemas = listOf("hello"))
        options.schemaIncluded("hello").shouldBeTrue()
        options.schemaIncluded("helloworld").shouldBeFalse()
        options.schemaIncluded("worldhello").shouldBeFalse()
        options.schemaIncluded("moon").shouldBeFalse()
    }

    @Test
    fun `included schemas are also excluded`() {
        val options = PgDiffOptions(
            schemas = listOf("hello", "world"),
            excludeSchemas = listOf("world")
        )
        options.schemaIncluded("hello").shouldBeTrue()
        options.schemaIncluded("world").shouldBeFalse()
    }

    @Test
    fun `schema exclusion pattern matching using regex`() {
        val options = PgDiffOptions(
            excludeSchemas = listOf("fe_.*")
        )
        options.schemaIncluded("fe").shouldBeTrue()
        options.schemaIncluded("fe_beta").shouldBeFalse()
        options.schemaIncluded("bo").shouldBeTrue()
    }

    @Test
    fun `schema inclusion pattern matching using regex`() {
        val options = PgDiffOptions(
            schemas = listOf("fe_.*")
        )
        options.schemaIncluded("fe").shouldBeFalse()
        options.schemaIncluded("fe_beta").shouldBeTrue()
        options.schemaIncluded("bo").shouldBeFalse()
    }
}