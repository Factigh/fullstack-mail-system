package de.thm.mni.mailbackend.mail

import java.time.OffsetDateTime

/**
 * Read-only information about the life of an email. Only the server changes it (rule E7).
 *
 * The constructor checks the rules M1 and M2 of `domain.md`, so an object of this class is
 * always in a valid state.
 *
 * @property createdAt time the email was created
 * @property updatedAt time the email was last changed
 * @property sentAt time the email was sent successfully, `null` while it was not sent
 * @property status current status of the email
 * @property source origin of the email
 */
data class EmailMetadata(
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val sentAt: OffsetDateTime?,
    val status: EmailStatus,
    val source: EmailSource,
) {
    init {
        require(!updatedAt.isBefore(createdAt)) {
            "updatedAt must not be earlier than createdAt (rule M1)"
        }
        require((sentAt != null) == (status == EmailStatus.SENT)) {
            "sentAt must be set if and only if the status is SENT (rule M2)"
        }
    }
}
