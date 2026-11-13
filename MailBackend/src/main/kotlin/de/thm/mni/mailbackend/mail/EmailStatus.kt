package de.thm.mni.mailbackend.mail

/**
 * Status of an email in its life cycle (see the workflow in `domain.md`).
 */
enum class EmailStatus {
    /** The email was created but not sent yet. It can be edited and sent. */
    DRAFT,

    /** The email was sent successfully. It is read-only (rule E6). */
    SENT,

    /** The last send attempt failed. The email can be edited and sent again. */
    ERROR,
}
