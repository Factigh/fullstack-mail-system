package de.thm.mni.mailbackend.mail.api

import de.thm.mni.mailbackend.mail.EmailService
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

/**
 * REST API for the emails of the current user (`/api/v1/emails`).
 *
 * The controller only translates between HTTP and the business layer: it converts DTOs into
 * domain objects, calls the [EmailService], and converts the result back into DTOs.
 * Business rules and storage are not its concern.
 *
 * @property emailService business logic for emails, injected by Spring
 */
@RestController
@RequestMapping("/api/v1/emails")
class EmailRestApi(private val emailService: EmailService) {

    /**
     * Lists one page of email summaries, newest first.
     *
     * Invalid values (for example `size=0` or `status=foo`) answer `400`.
     *
     * @param page zero-based page number
     * @param size number of emails per page, 1 to 100
     * @param status only emails with this status, or all if omitted
     * @param source only emails with this source, or all if omitted
     * @return the requested page
     */
    @GetMapping
    fun listEmails(
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) size: Int,
        @RequestParam(required = false) status: EmailStatusDto?,
        @RequestParam(required = false) source: EmailSourceDto?,
    ): EmailPage =
        emailService.list(status?.toDomain(), source?.toDomain(), page, size).toEmailPage()

    /**
     * Creates a new email with the status `draft` and the source `intern`.
     *
     * A `ResponseEntity` is used because the API contract requires a `Location` header,
     * which cannot be set with annotations alone.
     *
     * @param request writable data of the new email, validated by `@Valid`
     * @return `201 Created` with the created email and a `Location` header pointing to it
     */
    @PostMapping
    fun createEmail(@Valid @RequestBody request: EmailCreate): ResponseEntity<EmailDetail> {
        val email = emailService.create(request.toMessage(CURRENT_USER_EMAIL))
        return ResponseEntity
            .created(URI.create("/api/v1/emails/${email.id}"))
            .body(email.toDetail())
    }

    /**
     * Returns all data of one email.
     *
     * @param emailId identifier of the email
     * @return the email; an unknown id answers `404`
     */
    @GetMapping("/{emailId}")
    fun getEmail(@PathVariable emailId: UUID): EmailDetail =
        emailService.get(emailId).toDetail()

    /**
     * Replaces the writable data of an email.
     *
     * @param emailId identifier of the email
     * @param request complete writable data of the email, validated by `@Valid`
     * @return the updated email; an unknown id answers `404`, a sent email `409`
     */
    @PutMapping("/{emailId}")
    fun updateEmail(
        @PathVariable emailId: UUID,
        @Valid @RequestBody request: EmailUpdate,
    ): EmailDetail =
        emailService.update(emailId, request.toMessage(CURRENT_USER_EMAIL)).toDetail()

    /**
     * Deletes an email permanently.
     *
     * @param emailId identifier of the email
     */
    @DeleteMapping("/{emailId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteEmail(@PathVariable emailId: UUID) {
        emailService.delete(emailId)
    }

    private companion object {
        /** Address of the current user until authentication (JWT) is implemented. */
        const val CURRENT_USER_EMAIL = "alex.becker@mni.thm.de"
    }
}
