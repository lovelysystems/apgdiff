package cz.startnet.utils.pgdiff.schema

import java.io.PrintWriter

class PgOperator(name: String, position: Int) : DBObject("OPERATOR", name, position) {

    companion object {
        const val NONE = "NONE"
    }

    lateinit var functionName: String

    var leftType: String? = null
    var rightType: String? = null
    var comOp: String? = null
    var negOp: String? = null
    var resProc: String? = null
    var joinProc: String? = null
    var hashes: Boolean = false
    var merges: Boolean = false


    override fun quotedIdentifier(): String {
        return """${super.quotedIdentifier()} (${leftType ?: NONE}, ${rightType ?: NONE})"""
    }

    val signature: String
        get() {
            return quotedIdentifier()
        }

    fun sameSettings(other: PgOperator): Boolean {
        return comOp == other.comOp && negOp == other.negOp && resProc == other.resProc
                && joinProc == other.joinProc && hashes == other.hashes && merges == other.merges
    }

    fun creationSQL(writer: PrintWriter) {
        writer.print("CREATE OPERATOR ${super.quotedIdentifier()} (\n")

        val args = mutableListOf<String>()
        args.add("    FUNCTION = $functionName")
        leftType?.let {
            args.add("LEFTARG = $it")
        }

        rightType?.let {
            args.add("RIGHTARG = $it")
        }

        comOp?.let {
            args.add("COMMUTATOR = $it")
        }


        negOp?.let {
            args.add("NEGATOR = $it")
        }

        resProc?.let {
            args.add("RESTRICT = $it")
        }
        joinProc?.let {
            args.add("JOIN = $it")
        }

        if (hashes) {
            args.add("HASHES")
        }

        if (merges) {
            args.add("MERGES")
        }

        writer.println(args.joinToString(",\n    "))

        writer.println("    );")
        writer.println()

    }
}

