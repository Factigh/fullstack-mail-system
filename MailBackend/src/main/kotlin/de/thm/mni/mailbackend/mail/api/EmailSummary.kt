package de.thm.mni.mailbackend.mail.api

import java.time.OffsetDateTime
import java.util.UUID

/**
 * Short representation of an email in the mailbox overview.
 *
 * It has no content and no attachment metadata, so that a list does not load data it does
 * not show (no overfetching).
 *
 * @property id identifier of the email
 * @property from sender address
 * @property subject subject of the email
 * @property status current status of the email
 * @property source origin of the email
 * @property createdAt time the email was created
 * @property updatedAt time the email was last changed
 * @property sentAt time the email was sent successfully, `null` while it was not sent
 * @property attachmentCount number of attachments
 */
data class EmailSummary(
    val id: UUID,
    val from: String,
    val subject: String,
    val status: EmailStatusDto,
    val source: EmailSourceDto,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val sentAt: OffsetDateTime?,
    val attachmentCount: Int,
)
