package de.thm.mni.mailbackend.mail.api

import de.thm.mni.mailbackend.mail.Transmission
import de.thm.mni.mailbackend.mail.TransmissionOutcome

/*
 * Conversions between the API representation of send attempts and the domain classes.
 */

/** Converts a domain send attempt into its API representation. */
fun Transmission.toDto(): TransmissionDto =
    TransmissionDto(
        id = id,
        emailId = emailId,
        outcome = outcome.toDto(),
        attemptedAt = attemptedAt,
        failureReason = failureReason,
    )

/** Converts a domain outcome into its API representation. */
fun TransmissionOutcome.toDto(): TransmissionOutcomeDto =
    when (this) {
        TransmissionOutcome.SUCCEEDED -> TransmissionOutcomeDto.SUCCEEDED
        TransmissionOutcome.FAILED -> TransmissionOutcomeDto.FAILED
    }
