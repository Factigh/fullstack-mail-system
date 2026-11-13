package de.thm.mni.mailbackend.mail

/**
 * Connection to the external mail server, which is implemented by another team.
 *
 * The business layer only knows this interface. The project uses a mock implementation
 * ([MockMailTransmissionGateway]); a real implementation can replace it later without any
 * change in the business layer (dependency inversion).
 */
interface MailTransmissionGateway {

    /**
     * Hands an email to the mail server.
     *
     * @param email the email to send
     * @return whether the mail server accepted the email
     */
    fun send(email: Email): DeliveryResult
}

/**
 * Answer of the mail server to one email.
 */
sealed interface DeliveryResult {

    /** The mail server accepted the email. */
    data object Delivered : DeliveryResult

    /**
     * The mail server rejected the email.
     *
     * @property reason why the email was rejected; it is shown to the user
     */
    data class Rejected(val reason: String) : DeliveryResult
}
