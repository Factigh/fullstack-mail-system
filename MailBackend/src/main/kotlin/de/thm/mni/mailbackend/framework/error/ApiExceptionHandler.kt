package de.thm.mni.mailbackend.framework.error

import org.slf4j.LoggerFactory
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
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.net.URI

/**
 * Translates exceptions of all controllers into ProblemDetail error responses (RFC 9457).
 *
 * The standard Spring MVC errors (for example `404`, `405`, `415`) are handled by the base class
 * [ResponseEntityExceptionHandler]. This class adds the rules of `openapi.yaml`:
 *
 * - A request body that cannot be read into the request schema answers `400`.
 * - A request body that was read but violates validation rules answers `422` with one entry
 *   per invalid property in `errors`.
 * - Every other exception answers `500` without internal details and is logged.
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
    }
}
