package cz.startnet.utils.pgdiff

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import org.testcontainers.utility.DockerImageName
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintWriter


class VanillaDBContainer(imageName: String) :
    GenericContainer<VanillaDBContainer>(DockerImageName.parse(imageName)) {

    override fun configure() {
        withNetworkAliases("postgres")
        withEnv(
            mapOf(
                "PGUSER" to "postgres",
                "POSTGRES_PASSWORD" to "postgres",
            )
        )
        withNetwork(Network.newNetwork()).apply {
            setWaitStrategy(LogMessageWaitStrategy().withRegEx(".*database system is ready to accept connections.*"))
        }
        super.configure()
    }

    fun createDB(dbName: String, setupFile: String) {
        execInContainer("createdb", dbName).let {
            if (it.exitCode != 0) {
                throw error(it.stderr)
            }
        }
        runFile(dbName, setupFile)
    }

    fun runFile(dbName: String, fileName: String) {
        execInContainer(
            "psql",
            "-v", "ON_ERROR_STOP=1",
            "-f", fileName, dbName
        ).let {
            if (it.exitCode != 0) {
                throw error(it.stderr)
            }
        }

    }

    fun dumpDB(dbName: String): String {
        return execInContainer("pg_dump", "-s", "-d", dbName).let {
            it.exitCode shouldBe 0
            it.stdout
        }
    }


}

fun PgDiff.createDiff(oldDump: String, newDump: String): String {
    val diffInput = ByteArrayOutputStream()
    val writer = PrintWriter(diffInput)
    val arguments = PgDiffArguments()
    PgDiff.createDiff(
        writer, arguments,
        oldDump.byteInputStream(),
        newDump.byteInputStream()
    )
    writer.close()
    return diffInput.toString()
}

class DBTest {

    val testFileDir = File("src/test/resources/pgdiff_test_files")

    val dbContainer = VanillaDBContainer("postgres:12.6").withFileSystemBind(
        testFileDir.absolutePath, "/testfiles"
    )

    @BeforeAll
    fun setUp() {
        dbContainer.start()
        dbContainer.isRunning.shouldBeTrue()
        // load the original
        Thread.sleep(100) // TODO: get rid of this sleep
        dbContainer.execInContainer("psql", "-c",
            "create role dv; create role admin; create role anonymous; create role asi; create role webuser; create role manager")
    }

    @AfterAll
    fun tearDown() {
        dbContainer.stop()
    }

    private val testFilePattern = Regex("^(.*)_original.sql")

    fun sqlFiles() = testFileDir.list()!!.map {
        testFilePattern.matchEntire(it)?.groups?.get(1)?.value
    }.filterNotNull().sorted()


    @ParameterizedTest
    @MethodSource("sqlFiles")
    fun migratedDBEqualsNew(name: String) {

        // val name = "add_cluster"

        val oldDB = "${name}_old"
        val newDB = "${name}_new"

        dbContainer.createDB(oldDB, "/testfiles/${name}_original.sql")
        // dump the original
        val oldDump = dbContainer.dumpDB(oldDB)

        // load the new
        dbContainer.createDB(newDB, "/testfiles/${name}_new.sql")
        // dump the new
        val newDump = dbContainer.dumpDB(newDB)


        // run diff on both dumps

        val firstDiff = PgDiff.createDiff(oldDump, newDump)
        val diffFile = testFileDir.resolve("${name}_diff.sql")

        // write the diff file
        if (!diffFile.exists() || diffFile.readText() != firstDiff) {
            diffFile.writeText(firstDiff)
        }


        // apply diff to original
        dbContainer.runFile(oldDB, "/testfiles/${diffFile.name}")

        // dmp the original with applied diff
        val migratedDump = dbContainer.dumpDB(oldDB)

        if (migratedDump != newDump) {
            // could be still valid, because of column ordering etc.
            // try with to do a diff, if this is empty, it is ok
            val diff = PgDiff.createDiff(migratedDump, newDump)
            if (diff.isNotBlank()) {
                // there is a diff  let the assertion raise
                migratedDump shouldBe newDump
            }
        }

    }

}