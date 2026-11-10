package de.thm.mni.mailbackend.mail.api

/**
 * Request body to create a new email (`POST /api/v1/emails`).
 *
 * Only the writable fields of an email are part of this class. Server-controlled data such as
 * the id, the status, or the timestamps is set by the server.
 *
 * Omitted recipient lists are treated as empty lists.
 *
 * @property from sender address; if omitted, the address of the current user is used
 * @property to recipient addresses
 * @property cc carbon copy addresses
 * @property bcc blind carbon copy addresses
 * @property replyTo addresses that replies should be sent to
 * @property subject subject of the email
 * @property content plain-text body of the email
 */
data class EmailCreate(
    val from: String? = null,
    val to: List<String> = emptyList(),
    val cc: List<String> = emptyList(),
    val bcc: List<String> = emptyList(),
    val replyTo: List<String> = emptyList(),
    val subject: String,
    val content: String,
)
