package de.thm.mni.mailbackend.mail.api

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Result of a send attempt as it appears in the API.
 *
 * The JSON values are lowercase (`succeeded`, `failed`), as defined in `openapi.yaml`.
 */
enum class TransmissionOutcomeDto {
    /** The mail server accepted the email. */
    @JsonProperty("succeeded")
    SUCCEEDED,

    /** The mail server did not accept the email. */
    @JsonProperty("failed")
    FAILED,
}

/**
 * Response body with one send attempt (schema `Transmission` in `openapi.yaml`).
 *
 * @property id identifier of the attempt
 * @property emailId identifier of the email that was sent
 * @property outcome result of the attempt
 * @property attemptedAt time of the attempt
 * @property failureReason why the attempt failed, `null` if it succeeded
 */
data class TransmissionDto(
    val id: UUID,
    val emailId: UUID,
    val outcome: TransmissionOutcomeDto,
    val attemptedAt: OffsetDateTime,
    val failureReason: String?,
)
