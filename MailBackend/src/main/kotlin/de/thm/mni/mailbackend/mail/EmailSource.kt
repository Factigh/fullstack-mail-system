package de.thm.mni.mailbackend.mail

/**
 * Origin of an email.
 */
enum class EmailSource {
    /** The email was written in this system. */
    INTERN,

    /** The email was received from outside. */
    EXTERN,
}
