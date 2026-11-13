package de.thm.mni.mailbackend.mail

/**
 * The writable part of an email: addresses, subject, and text.
 *
 * Everything a user can create or change is in this class. Data controlled by the server
 * is in [EmailMetadata].
 *
 * @property from sender address
 * @property to recipient addresses
 * @property cc carbon copy addresses
 * @property bcc blind carbon copy addresses
 * @property replyTo addresses that replies should be sent to
 * @property subject subject of the email
 * @property content plain-text body of the email
 */
data class EmailMessage(
    val from: String,
    val to: List<String>,
    val cc: List<String>,
    val bcc: List<String>,
    val replyTo: List<String>,
    val subject: String,
    val content: String,
) {
    /** Number of addresses in `to`, `cc`, and `bcc` together. */
    val recipientCount: Int
        get() = to.size + cc.size + bcc.size

    companion object {
        /** `to`, `cc`, and `bcc` together have at most 100 addresses (rule E2). */
        const val MAX_RECIPIENTS = 100
    }
}
