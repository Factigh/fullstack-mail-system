package de.thm.mni.mailbackend.framework.error

import org.slf4j.LoggerFactory
import org.springframework.beans.TypeMismatchException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.net.URI

/**
 * Translates exceptions of all controllers into ProblemDetail error responses (RFC 9457).
 *
 * The standard Spring MVC errors (for example `405`, `415`, or a path variable that is not a
 * UUID) are handled by the base class [ResponseEntityExceptionHandler]. This class adds the
 * rules of `openapi.yaml`:
 *
 * - A request body that cannot be read into the request schema answers `400`.
 * - Invalid query or path parameters (for example `size=0`, `status=foo`, or an id that is not
 *   a UUID) answer `400`.
 * - A request body that was read but violates validation rules answers `422` with one entry
 *   per invalid property in `errors`.
 * - A [NotFoundException] of the business layer answers `404`.
 * - A [ConflictException] of the business layer answers `409`.
 * - A [ContentTooLargeException] of the business layer answers `413`.
 * - A [RuleViolationException] of the business layer answers `422`.
 * - Every other exception answers `500` without internal details and is logged.
 *
 * All responses get the same titles, see [createResponseEntity].
 */
@RestControllerAdvice
class ApiExceptionHandler : ResponseEntityExceptionHandler() {

    private val log = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

    /**
     * Handles violations of the validation annotations of a request body (`@Valid`).
     */
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        val fieldErrors = ex.bindingResult.fieldErrors.map { error ->
            mapOf("detail" to (error.defaultMessage ?: "Invalid value."), "pointer" to "/${error.field}")
        }
        val globalErrors = ex.bindingResult.globalErrors.map { error ->
            mapOf("detail" to (error.defaultMessage ?: "Invalid request."), "pointer" to "")
        }
        val errors = fieldErrors + globalErrors
        log.warn("Rejected request to {} with {} validation error(s)", path(request), errors.size)

        val problem = createProblem(
            UNPROCESSABLE_ENTITY,
            "UnprocessableEntity",
            "The proposed content is syntactically correct but violates validation rules.",
            request,
        )
        problem.setProperty("errors", errors)
        return handleExceptionInternal(ex, problem, headers, UNPROCESSABLE_ENTITY, request)
    }

    /**
     * Handles request bodies that cannot be read: malformed JSON, wrong types, missing required
     * properties, explicit `null`, or unknown properties.
     */
    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        log.warn("Rejected unreadable request body at {}: {}", path(request), ex.mostSpecificCause.message)

        val problem = createProblem(
            HttpStatus.BAD_REQUEST,
            "BadRequest",
            "The request body could not be read. Check the JSON syntax, the property names, and the required properties.",
            request,
        )
        return handleExceptionInternal(ex, problem, headers, HttpStatus.BAD_REQUEST, request)
    }

    /**
     * Handles violations of validation annotations on query or path parameters,
     * for example `@Min(1)` on the page size.
     */
    override fun handleHandlerMethodValidationException(
        ex: HandlerMethodValidationException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        log.warn("Rejected request to {} with invalid parameters", path(request))

        val problem = createProblem(
            HttpStatus.BAD_REQUEST,
            "BadRequest",
            "One or more request parameters are invalid.",
            request,
        )
        return handleExceptionInternal(ex, problem, headers, HttpStatus.BAD_REQUEST, request)
    }

    /**
     * Handles query or path parameters with the wrong format, for example `?status=foo` or an
     * email id that is not a UUID.
     */
    override fun handleTypeMismatch(
        ex: TypeMismatchException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        log.warn("Rejected request to {} with a parameter of the wrong format: {}", path(request), ex.propertyName)

        val problem = createProblem(
            HttpStatus.BAD_REQUEST,
            "BadRequest",
            "The parameter '${ex.propertyName ?: "unknown"}' has an invalid value.",
            request,
        )
        return handleExceptionInternal(ex, problem, headers, HttpStatus.BAD_REQUEST, request)
    }

    /**
     * Handles resources that do not exist. The message was written by the business layer and
     * contains no internal details.
     */
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException, request: WebRequest): ResponseEntity<Any>? {
        log.info("Not found at {}: {}", path(request), ex.message)

        val problem = createProblem(
            HttpStatus.NOT_FOUND,
            "NotFound",
            ex.message ?: "The resource does not exist.",
            request,
        )
        return handleExceptionInternal(ex, problem, HttpHeaders(), HttpStatus.NOT_FOUND, request)
    }

    /**
     * Handles requests that conflict with the current state of a resource, for example
     * changing a sent email.
     */
    @ExceptionHandler(ConflictException::class)
    fun handleConflict(ex: ConflictException, request: WebRequest): ResponseEntity<Any>? {
        log.info("Conflict at {}: {}", path(request), ex.message)

        val problem = createProblem(
            HttpStatus.CONFLICT,
            "Conflict",
            ex.message ?: "The request conflicts with the current state of the resource.",
            request,
        )
        return handleExceptionInternal(ex, problem, HttpHeaders(), HttpStatus.CONFLICT, request)
    }

    /**
     * Handles broken business rules, for example too many attachments.
     */
    @ExceptionHandler(RuleViolationException::class)
    fun handleRuleViolation(ex: RuleViolationException, request: WebRequest): ResponseEntity<Any>? {
        log.info("Rule violation at {}: {}", path(request), ex.message)

        val problem = createProblem(
            UNPROCESSABLE_ENTITY,
            "UnprocessableEntity",
            ex.message ?: "The request violates a business rule.",
            request,
        )
        return handleExceptionInternal(ex, problem, HttpHeaders(), UNPROCESSABLE_ENTITY, request)
    }

    /**
     * Handles uploaded content that exceeds a size limit of the business layer.
     * Files that exceed the multipart limits of `application.yaml` are rejected earlier by
     * Spring and also answer `413`.
     */
    @ExceptionHandler(ContentTooLargeException::class)
    fun handleContentTooLarge(ex: ContentTooLargeException, request: WebRequest): ResponseEntity<Any>? {
        log.info("Content too large at {}: {}", path(request), ex.message)

        val problem = createProblem(
            CONTENT_TOO_LARGE,
            "PayloadTooLarge",
            ex.message ?: "The uploaded content is too large.",
            request,
        )
        return handleExceptionInternal(ex, problem, HttpHeaders(), CONTENT_TOO_LARGE, request)
    }

    /**
     * Last step of every handler, including the standard handlers of Spring. At this point the
     * ProblemDetail is complete, also for errors where Spring creates it itself (for example
     * `405` or `415`).
     *
     * It makes all error responses look the same: the title follows the names in `openapi.yaml`
     * (for example `UnsupportedMediaType` instead of `Unsupported Media Type`), and `instance`
     * is always the path of the request.
     */
    override fun createResponseEntity(
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        if (body is ProblemDetail) {
            TITLES[statusCode.value()]?.let { body.title = it }
            if (body.instance == null) {
                body.instance = URI.create(path(request))
            }
        }
        return super.createResponseEntity(body, headers, statusCode, request)
    }

    /**
     * Handles every exception that no other handler covers. The client only receives a general
     * message, the details are written to the log.
     */
    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception, request: WebRequest): ResponseEntity<Any>? {
        log.error("Unexpected error at {}", path(request), ex)

        val problem = createProblem(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "InternalServerError",
            "An unexpected error occurred. Please try again later.",
            request,
        )
        return handleExceptionInternal(ex, problem, HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request)
    }

    private fun createProblem(
        status: HttpStatusCode,
        title: String,
        detail: String,
        request: WebRequest,
    ): ProblemDetail =
        ProblemDetail.forStatusAndDetail(status, detail).apply {
            this.title = title
            instance = URI.create(path(request))
        }

    private fun path(request: WebRequest): String =
        (request as? ServletWebRequest)?.request?.requestURI ?: ""

    private companion object {
        val UNPROCESSABLE_ENTITY: HttpStatusCode = HttpStatusCode.valueOf(422)
        val CONTENT_TOO_LARGE: HttpStatusCode = HttpStatusCode.valueOf(413)

        /** Titles of the error responses per status code, as used in `openapi.yaml`. */
        val TITLES = mapOf(
            400 to "BadRequest",
            401 to "Unauthorized",
            404 to "NotFound",
            405 to "MethodNotAllowed",
            409 to "Conflict",
            413 to "PayloadTooLarge",
            415 to "UnsupportedMediaType",
            422 to "UnprocessableEntity",
            500 to "InternalServerError",
        )
    }
}
