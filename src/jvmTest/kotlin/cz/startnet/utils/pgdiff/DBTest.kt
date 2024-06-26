package cz.startnet.utils.pgdiff

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import org.testcontainers.containers.Container
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import org.testcontainers.utility.DockerImageName

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
        withTmpFs(mapOf("/var/lib/postgresql/data" to "rw"))
        withCommand("-c", "fsync=off")
        super.configure()
    }

    fun exec(vararg command: String): Container.ExecResult {
        return execInContainer(*command).let {
            if (it.exitCode != 0) {
                error(it.stderr)
            }
            it
        }
    }

    fun createDB(dbName: String, setupFile: String) {
        exec("createdb", dbName)
        runFile(dbName, setupFile)
    }

    fun dropDB(dbName: String) {
        exec("dropdb", dbName)
    }

    fun runFile(dbName: String, fileName: String) {
        exec(
            "psql",
            "-v", "ON_ERROR_STOP=1",
            "-f", fileName, dbName
        )
    }

    fun dumpDB(dbName: String): String {
        return exec("pg_dump", "-s", "-d", dbName).stdout
    }
}

@Execution(ExecutionMode.CONCURRENT)
class DBTest {

    private val dbContainer = VanillaDBContainer("postgres:14.6").withFileSystemBind(
        testFileDir.absolutePath, "/testfiles"
    )!!

    @BeforeAll
    fun setUp() {
        dbContainer.start()
        dbContainer.isRunning.shouldBeTrue()
        // load the original
        Thread.sleep(300) // TODO: get rid of this sleep
        dbContainer.exec(
            "psql", "-c",
            "create role dv; create role admin; create role anonymous; create role asi; create role webuser; create role manager"
        )
    }

    @AfterAll
    fun tearDown() {
        dbContainer.stop()
    }


    @ParameterizedTest
    @ArgumentsSource(SQLDiffFilesArgumentsProvider::class)
    fun migratedDBEqualsNew(testFiles: SQLDiffTestFiles) {

        val oldDB = "${testFiles.name}_old"
        val newDB = "${testFiles.name}_new"

        val pgDiff = PgDiff()

        dbContainer.createDB(oldDB, "/testfiles/${testFiles.old.name}")
        // dump the original
        val oldDump = dbContainer.dumpDB(oldDB)

        // load the new
        dbContainer.createDB(newDB, "/testfiles/${testFiles.new.name}")
        // dump the new
        val newDump = dbContainer.dumpDB(newDB)

        // run diff on both dumps
        val firstDiff = pgDiff.createDiff(oldDump, newDump)
        val df = testFiles.diff
        // write the diff file
        if (!df.exists() || df.readText() != firstDiff.script) {
            df.writeText(firstDiff.script)
        }

        // apply diff to original
        dbContainer.runFile(oldDB, "/testfiles/${df.name}")

        // dmp the original with applied diff
        val migratedDump = dbContainer.dumpDB(oldDB)

        if (migratedDump != newDump) {
            // could be still valid, because of column ordering etc.
            // try with to do a diff, if this is empty, it is ok
            val diff = pgDiff.createDiff(migratedDump, newDump)
            if (diff.script.isNotBlank()) {
                // there is a diff  let the assertion raise
                migratedDump shouldBe newDump
            }
        }
        dbContainer.dropDB(oldDB)
        dbContainer.dropDB(newDB)
    }
}
