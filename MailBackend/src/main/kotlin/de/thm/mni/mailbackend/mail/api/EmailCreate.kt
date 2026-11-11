package de.thm.mni.mailbackend.mail.api

import de.thm.mni.mailbackend.mail.EmailAddressRules
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * Request body to create a new email (`POST /api/v1/emails`).
 *
 * Only the writable fields of an email are part of this class. Server-controlled data such as
 * the id, the status, or the timestamps is set by the server.
 *
 * Omitted recipient lists are treated as empty lists. The limits follow `domain.md`.
 *
 * @property from sender address; if omitted, the address of the current user is used
 * @property to recipient addresses
 * @property cc carbon copy addresses
 * @property bcc blind carbon copy addresses
 * @property replyTo addresses that replies should be sent to
 * @property subject subject of the email
 * @property content plain-text body of the email
 */
@AtLeastOneRecipient
data class EmailCreate(
    @field:Pattern(regexp = EmailAddressRules.PATTERN, message = "Has to be a valid email address.")
    @field:Size(max = EmailAddressRules.MAX_LENGTH, message = "Has at most 254 characters.")
    val from: String? = null,

    @field:Size(max = 50, message = "Has at most 50 addresses.")
    @field:ValidAddressList
    override val to: List<String> = emptyList(),

    @field:Size(max = 50, message = "Has at most 50 addresses.")
    @field:ValidAddressList
    override val cc: List<String> = emptyList(),

    @field:Size(max = 50, message = "Has at most 50 addresses.")
    @field:ValidAddressList
    override val bcc: List<String> = emptyList(),

    @field:Size(max = 50, message = "Has at most 50 addresses.")
    @field:ValidAddressList
    val replyTo: List<String> = emptyList(),

    @field:NotBlank(message = "Subject must not be blank.")
    @field:Size(max = 255, message = "Subject has at most 255 characters.")
    val subject: String,

    @field:NotBlank(message = "Content must not be blank.")
    @field:Size(max = 100_000, message = "Content has at most 100000 characters.")
    val content: String,
) : HasRecipients
