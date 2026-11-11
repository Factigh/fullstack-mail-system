package de.thm.mni.mailbackend.mail.api

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * REST API for the emails of the current user (`/api/v1/emails`).
 *
 * At this stage there is no service and no repository: the controller builds the response
 * directly from the request and does not store anything.
 */
@RestController
@RequestMapping("/api/v1/emails")
class EmailRestApi {

    /**
     * Creates a new email with the status `draft` and the source `intern`.
     *
     * A `ResponseEntity` is used because the API contract requires a `Location` header,
     * which cannot be set with annotations alone.
     *
     * `@Valid` triggers the validation annotations of [EmailCreate]. An invalid request body
     * never reaches this method; it is answered by the error handler.
     *
     * @param request writable data of the new email
     * @return `201 Created` with the created email and a `Location` header pointing to it
     */
    @PostMapping
    fun createEmail(@Valid @RequestBody request: EmailCreate): ResponseEntity<EmailDetail> {
        val now = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        val email = EmailDetail(
            id = UUID.randomUUID(),
            from = request.from ?: CURRENT_USER_EMAIL,
            to = request.to,
            cc = request.cc,
            bcc = request.bcc,
            replyTo = request.replyTo,
            subject = request.subject,
            content = request.content,
            status = EmailStatus.DRAFT,
            source = EmailSource.INTERN,
            createdAt = now,
            updatedAt = now,
            sentAt = null,
            attachments = emptyList(),
        )
        return ResponseEntity
            .created(URI.create("/api/v1/emails/${email.id}"))
            .body(email)
    }

    private companion object {
        /** Address of the current user until authentication (JWT) is implemented. */
        const val CURRENT_USER_EMAIL = "alex.becker@mni.thm.de"
    }
}
