/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff

import java.io.PrintWriter
import java.nio.charset.Charset

/**
 * Contains parsed command line arguments.
 *
 * @author fordfrog
 */
class PgDiffArguments {
    /**
     * Getter for [.inCharsetName].
     *
     * @return [.inCharsetName]
     */
    /**
     * Setter for [.inCharsetName].
     *
     * @param inCharsetName [.inCharsetName]
     */
    /**
     * Input file charset name.
     */
    var inCharsetName = "UTF-8"
    /**
     * Getter for [.newDumpFile].
     *
     * @return [.newDumpFile]
     */
    /**
     * Setter for [.newDumpFile].
     *
     * @param newDumpFile [.newDumpFile]
     */
    /**
     * Path to the new dump file.
     */
    var newDumpFile: String? = null
    /**
     * Getter for [.oldDumpFile].
     *
     * @return [.oldDumpFile]
     */
    /**
     * Setter for [.oldDumpFile].
     *
     * @param oldDumpFile [.oldDumpFile]
     */
    /**
     * Path to the original dump file.
     */
    var oldDumpFile: String? = null
    /**
     * Getter for [.outCharsetName].
     *
     * @return [.outCharsetName]
     */
    /**
     * Setter for [.outCharsetName].
     *
     * @param outCharsetName [.outCharsetName]
     */
    /**
     * Output file charset name.
     */
    var outCharsetName = "UTF-8"
    /**
     * Getter for [.addDefaults].
     *
     * @return [.addDefaults]
     */
    /**
     * Setter for [.addDefaults].
     *
     * @param addDefaults [.addDefaults]
     */
    /**
     * Whether DEFAULT ... should be added in case new column has NOT NULL
     * constraint. The default value is dropped later.
     */
    var isAddDefaults = false
    /**
     * Getter for [.addTransaction].
     *
     * @return [.addTransaction]
     */
    /**
     * Setter for [.addTransaction].
     *
     * @param addTransaction [.addTransaction]
     */
    /**
     * Whether to enclose all statements in transaction.
     */
    var isAddTransaction = false
    /**
     * Getter for [.ignoreFunctionWhitespace].
     *
     * @return [.ignoreFunctionWhitespace]
     */
    /**
     * Setter for [.ignoreFunctionWhitespace].
     *
     * @param ignoreFunctionWhitespace [.ignoreFunctionWhitespace]
     */
    /**
     * Whether to ignore whitespace while comparing content of functions.
     */
    var isIgnoreFunctionWhitespace = false
    /**
     * Getter for [.version].
     *
     * @return [.version]
     */
    /**
     * Setter for [.version].
     *
     * @param version [.version]
     */
    /**
     * Whether to display apgdiff version.
     */
    var isVersion = false
    /**
     * Getter for [.outputIgnoredStatements].
     *
     * @return [.outputIgnoredStatements]
     */
    /**
     * Setter for [.outputIgnoredStatements].
     *
     * @param outputIgnoredStatements [.outputIgnoredStatements]
     */
    /**
     * Whether to output information about ignored statements.
     */
    var isOutputIgnoredStatements = false
    /**
     * Getter for [.listCharsets].
     *
     * @return [.listCharsets]
     */
    /**
     * Setter for [.listCharsets].
     *
     * @param listCharsets [.listCharsets]
     */
    /**
     * Whether to list supported charsets.
     */
    var isListCharsets = false
    /**
     * Getter for [.ignoreSlonyTriggers].
     *
     * @return [.ignoreSlonyTriggers]
     */
    /**
     * Setter for [.ignoreSlonyTriggers].
     *
     * @param ignoreSlonyTriggers [.ignoreSlonyTriggers]
     */
    /**
     * Whether Slony triggers should be ignored.
     */
    var isIgnoreSlonyTriggers = false
    /**
     * Getter for [.ignoreSchemaCreation].
     *
     * @return [.ignoreSchemaCreation]
     */
    /**
     * Setter for [.ignoreSchemaCreation].
     *
     * @param ignoreSchemaCreation [.ignoreSchemaCreation]
     */
    /**
     * Whether Schema creation should be ignored.
     */
    var isIgnoreSchemaCreation = false

    /**
     * Drop If Exists and Create If Exists where possible
     */
    private val useIfExists = false

    /**
     * Parses command line arguments or outputs instructions.
     *
     * @param writer writer to be used for info output
     * @param args   array of arguments
     *
     * @return true if arguments were parsed and execution can continue,
     * otherwise false
     */
    fun parse(writer: PrintWriter, args: Array<String>): Boolean {
        var success = true
        val argsLength: Int
        argsLength = if (args.size >= 2) {
            args.size - 2
        } else {
            args.size
        }
        var i = 0
        while (i < argsLength) {
            if ("--add-defaults" == args[i]) {
                isAddDefaults = true
            } else if ("--add-transaction" == args[i]) {
                isAddTransaction = true
            } else if ("--ignore-function-whitespace" == args[i]) {
                isIgnoreFunctionWhitespace = true
            } else if ("--ignore-slony-triggers" == args[i]) {
                isIgnoreSlonyTriggers = true
            } else if ("--ignore-schema-creation" == args[i]) {
                isIgnoreSchemaCreation = true
            } else if ("--in-charset-name" == args[i]) {
                inCharsetName = args[i + 1]
                i++
            } else if ("--list-charsets" == args[i]) {
                isListCharsets = true
            } else if ("--out-charset-name" == args[i]) {
                outCharsetName = args[i + 1]
                i++
            } else if ("--output-ignored-statements" == args[i]) {
                isOutputIgnoredStatements = true
            } else if ("--version" == args[i]) {
                isVersion = true
            } else if ("--drop-if-exists" == args[i]) {
                PgDiffUtils.setUseExists(true)
            } else {
                writer.print(Resources.getString("ErrorUnknownOption"))
                writer.print(": ")
                writer.println(args[i])
                success = false
                break
            }
            i++
        }
        if (args.size == 1 && isVersion) {
            printVersion(writer)
            success = false
        } else if (args.size == 1 && isListCharsets) {
            listCharsets(writer)
            success = false
        } else if (args.size < 2) {
            printUsage(writer)
            success = false
        } else if (success) {
            oldDumpFile = args[args.size - 2]
            newDumpFile = args[args.size - 1]
        }
        return success
    }

    /**
     * Prints program usage.
     *
     * @param writer writer to print the usage to
     */
    private fun printUsage(writer: PrintWriter) {
        writer.println(
            Resources.getString("UsageHelp").replace("\${tab}", "\t")
        )
    }

    /**
     * Prints program version.
     *
     * @param writer writer to print the usage to
     */
    private fun printVersion(writer: PrintWriter) {
        writer.print(Resources.getString("Version"))
        writer.print(": ")
        writer.println(Resources.getString("VersionNumber"))
    }

    /**
     * Lists supported charsets.
     *
     * @param writer writer
     */
    private fun listCharsets(writer: PrintWriter) {
        val charsets = Charset.availableCharsets()
        for (name in charsets.keys) {
            writer.println(name)
        }
    }
}