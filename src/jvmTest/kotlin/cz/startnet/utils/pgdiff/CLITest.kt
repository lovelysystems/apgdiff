package cz.startnet.utils.pgdiff

import com.github.ajalt.clikt.core.main
import io.kotest.matchers.shouldBe
import kotlinx.io.Buffer
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import org.junit.jupiter.api.Test
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempDirectory

class CLITest {

    @Test
    fun testOutputFileIsWritten() {
        val tmp = Path(createTempDirectory().absolutePathString())
        val p = SystemFileSystem.resolve(Path("./src/jvmTest/resources/pgdiff_test_files"))
        val orig = Path(p, "add_column_original.sql")
        val new = Path(p, "add_column_new.sql")
        val out = Path(tmp, "out.txt")

        val args = arrayOf(orig.toString(), new.toString(), "--out-file", out.toString())
        CLI().main(args)
        val expectedDiff = Path(p, "add_column_diff.sql").readText()
        out.readText().shouldBe(expectedDiff)
    }

}

private fun Path.readText(): String {
    val source = SystemFileSystem.source(this)
    val buffer = Buffer()
    source.buffered().readAtMostTo(buffer, 1024)
    return buffer.readString()
}
