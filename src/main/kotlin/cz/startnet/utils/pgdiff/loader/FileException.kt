/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.loader

/**
 * Exception thrown if problem occurred while reading or writing file.
 *
 * @author fordfrog
 */
class FileException : RuntimeException {
    /**
     * Constructs an instance of `FileException` with the specified detail
     * message.
     *
     * @param msg the detail message
     */
    constructor(msg: String?) : super(msg)

    /**
     * Constructs an instance of `FileException` with the specified detail
     * message.
     *
     * @param msg   the detail message
     * @param cause cause of the exception
     */
    constructor(msg: String?, cause: Throwable?) : super(msg, cause)

    companion object {
        /**
         * Serial version uid.
         */
        private const val serialVersionUID = 1L
    }
}