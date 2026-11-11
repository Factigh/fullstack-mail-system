package de.thm.mni.mailbackend.mail.api

/**
 * A request body that carries the recipient lists of an email.
 *
 * Both the creation and the update of an email implement this interface, so that the
 * recipient rule ([AtLeastOneRecipient]) is written only once.
 */
interface HasRecipients {
    /** Recipient addresses. */
    val to: List<String>

    /** Carbon copy addresses. */
    val cc: List<String>

    /** Blind carbon copy addresses. */
    val bcc: List<String>
}
