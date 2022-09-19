package net.dongliu.apk.parser.exception

/**
 * throwed when parse failed.
 *
 * @author dongliu
 */
class ParserException : RuntimeException {
    constructor(msg: String?) : super(msg) {}
    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
    constructor(cause: Throwable?) : super(cause) {}
    constructor(
        message: String?, cause: Throwable?, enableSuppression: Boolean,
        writableStackTrace: Boolean
    ) : super(message, cause, enableSuppression, writableStackTrace) {
    }

    constructor() {}

    companion object {
        private const val serialVersionUID = -669279149141454276L
    }
}