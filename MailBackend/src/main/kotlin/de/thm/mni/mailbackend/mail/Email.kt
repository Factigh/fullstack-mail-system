package de.thm.mni.mailbackend.mail

import java.util.UUID

/**
 * An email of the domain model (see `domain.md`).
 *
 * This class knows nothing about HTTP, JSON, or databases. The API layer converts it into
 * DTOs, the repository layer stores it.
 *
 * @property id identifier of the email
 * @property message writable part of the email (addresses, subject, text)
 * @property metadata read-only information controlled by the server
 */
data class Email(
    val id: UUID,
    val message: EmailMessage,
    val metadata: EmailMetadata,
) {
    /** `true` if the email was sent and can no longer be changed (rule E6). */
    val isReadOnly: Boolean
        get() = metadata.status == EmailStatus.SENT
}
