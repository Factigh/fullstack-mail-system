package de.thm.mni.mailbackend.mail.api

import java.time.OffsetDateTime
import java.util.UUID

/**
 * Response body with all data of one email, including metadata and attachment metadata.
 *
 * @property id identifier of the email
 * @property from sender address
 * @property to recipient addresses
 * @property cc carbon copy addresses
 * @property bcc blind carbon copy addresses
 * @property replyTo addresses that replies should be sent to
 * @property subject subject of the email
 * @property content plain-text body of the email
 * @property status current status of the email
 * @property source origin of the email
 * @property createdAt time the email was created
 * @property updatedAt time the email was last changed
 * @property sentAt time the email was sent successfully, `null` while it was not sent
 * @property attachments metadata of all attachments of the email
 */
data class EmailDetail(
    val id: UUID,
    val from: String,
    val to: List<String>,
    val cc: List<String>,
    val bcc: List<String>,
    val replyTo: List<String>,
    val subject: String,
    val content: String,
    val status: EmailStatusDto,
    val source: EmailSourceDto,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val sentAt: OffsetDateTime?,
    val attachments: List<AttachmentMetadata>,
)
