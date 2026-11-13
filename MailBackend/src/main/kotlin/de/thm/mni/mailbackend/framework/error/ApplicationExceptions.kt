package de.thm.mni.mailbackend.framework.error

/*
 * Base classes for errors of the business layer.
 *
 * The business layer throws subclasses of them without knowing about HTTP. The
 * ApiExceptionHandler translates them into status codes. Their messages may be shown to the
 * client, so they must never contain internal details.
 */

/**
 * A requested resource does not exist. Translated into `404 Not Found`.
 *
 * @param message description of the error
 */
open class NotFoundException(message: String) : RuntimeException(message)

/**
 * A request conflicts with the current state of a resource. Translated into `409 Conflict`.
 *
 * @param message description of the error
 */
open class ConflictException(message: String) : RuntimeException(message)

/**
 * A request breaks a business rule. Translated into `422 Unprocessable Entity`.
 *
 * @param message description of the broken rule
 */
open class RuleViolationException(message: String) : RuntimeException(message)

/**
 * Uploaded content exceeds a size limit. Translated into `413 Payload Too Large`.
 *
 * @param message description of the exceeded limit
 */
open class ContentTooLargeException(message: String) : RuntimeException(message)
