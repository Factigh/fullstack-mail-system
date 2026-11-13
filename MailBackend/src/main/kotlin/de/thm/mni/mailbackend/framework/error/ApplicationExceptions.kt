package de.thm.mni.mailbackend.framework.error

/**
 * Base class for errors where a requested resource does not exist.
 *
 * The business layer throws subclasses of it without knowing about HTTP. The
 * [ApiExceptionHandler] translates them into `404 Not Found`.
 *
 * @param message description of the error that may be shown to the client
 */
open class NotFoundException(message: String) : RuntimeException(message)

/**
 * Base class for errors where a request conflicts with the current state of a resource.
 *
 * The [ApiExceptionHandler] translates subclasses into `409 Conflict`.
 *
 * @param message description of the error that may be shown to the client
 */
open class ConflictException(message: String) : RuntimeException(message)
