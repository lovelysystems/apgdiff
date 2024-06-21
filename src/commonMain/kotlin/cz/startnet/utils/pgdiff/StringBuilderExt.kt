package cz.startnet.utils.pgdiff

fun StringBuilder.println(line: String?) = appendLine(line)
fun StringBuilder.println(line: Char) = appendLine(line)
fun StringBuilder.println() = appendLine()
fun StringBuilder.println(line: CharSequence) = appendLine(line)
fun StringBuilder.print(line: Int) = append(line)
fun StringBuilder.print(line: String?) = append(line)
fun StringBuilder.print(line: CharSequence) = append(line)
fun StringBuilder.print(line: Char) = append(line)
fun StringBuilder.write(line: String) = append(line)

fun StringBuilder.printStmt(vararg parts: String) {
    val stmt = parts.joinToString(" ", postfix = ";") { it.trimStart() }
    this.appendLine(stmt)
}
