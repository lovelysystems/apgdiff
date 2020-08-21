/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.parsers

/**
 * Thrown if parsing problem occurred.
 *
 * @author fordfrog
 */
class ParserException : RuntimeException {
    /**
     * Creates a new instance of `ParserException` without detail message.
     */
    constructor() {}

    /**
     * Constructs an instance of `ParserException` with the specified
     * detail message.
     *
     * @param msg the detail message
     */
    constructor(msg: String?) : super(msg) {}

    /**
     * Constructs an instance of `ParserException` with the specified
     * detail message.
     *
     * @param msg   the detail message
     * @param cause cause of the exception
     */
    constructor(msg: String?, cause: Throwable?) : super(msg, cause) {}

    companion object {
        /**
         * Serial version uid.
         */
        private const val serialVersionUID = 1L
    }
}