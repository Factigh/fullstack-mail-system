package de.thm.mni.mailbackend.mail

import java.time.OffsetDateTime
import java.util.UUID

/**
 * Result of one send attempt.
 */
enum class TransmissionOutcome {
    /** The mail server accepted the email. */
    SUCCEEDED,

    /** The mail server did not accept the email. */
    FAILED,
}

/**
 * One attempt to send an email (see `domain.md`).
 *
 * An email can have several attempts: an email with the status [EmailStatus.ERROR] can be
 * sent again. The constructor checks rule T1, so an object is always in a valid state.
 *
 * @property id identifier of the attempt
 * @property emailId identifier of the email that was sent
 * @property outcome result of the attempt
 * @property attemptedAt time of the attempt
 * @property failureReason why the attempt failed, `null` if it succeeded
 */
data class Transmission(
    val id: UUID,
    val emailId: UUID,
    val outcome: TransmissionOutcome,
    val attemptedAt: OffsetDateTime,
    val failureReason: String?,
) {
    init {
        require((failureReason != null) == (outcome == TransmissionOutcome.FAILED)) {
            "failureReason must be set if and only if the outcome is FAILED (rule T1)"
        }
    }
}
