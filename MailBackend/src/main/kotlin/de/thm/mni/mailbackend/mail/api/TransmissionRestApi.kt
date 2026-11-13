package de.thm.mni.mailbackend.mail.api

import de.thm.mni.mailbackend.mail.TransmissionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

/**
 * REST API for the send attempts of one email (`/api/v1/emails/{emailId}/transmissions`).
 *
 * Sending is modeled as a resource, as shown in the lecture: instead of
 * `POST /emails/{emailId}/send`, a client creates a transmission.
 *
 * @property transmissionService business logic for sending, injected by Spring
 */
@RestController
@RequestMapping("/api/v1/emails/{emailId}/transmissions")
class TransmissionRestApi(private val transmissionService: TransmissionService) {

    /**
     * Lists all send attempts of an email, the newest first.
     *
     * @param emailId identifier of the email
     * @return the send attempts; an unknown email answers `404`
     */
    @GetMapping
    fun listTransmissions(@PathVariable emailId: UUID): List<TransmissionDto> =
        transmissionService.list(emailId).map { it.toDto() }

    /**
     * Sends an email synchronously. The request has no body.
     *
     * A failed attempt is also answered with `201`: the HTTP request succeeded, the mail
     * server did not. The client checks `outcome`.
     *
     * @param emailId identifier of the email
     * @return `201 Created` with the send attempt and a `Location` header; a sent or received
     * email answers `409`
     */
    @PostMapping
    fun createTransmission(@PathVariable emailId: UUID): ResponseEntity<TransmissionDto> {
        val transmission = transmissionService.send(emailId)
        return ResponseEntity
            .created(URI.create("/api/v1/emails/$emailId/transmissions/${transmission.id}"))
            .body(transmission.toDto())
    }

    /**
     * Returns one send attempt.
     *
     * @param emailId identifier of the email
     * @param transmissionId identifier of the send attempt
     * @return the send attempt; an attempt of another email answers `404`
     */
    @GetMapping("/{transmissionId}")
    fun getTransmission(
        @PathVariable emailId: UUID,
        @PathVariable transmissionId: UUID,
    ): TransmissionDto =
        transmissionService.get(emailId, transmissionId).toDto()
}
