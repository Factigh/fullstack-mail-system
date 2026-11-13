package de.thm.mni.mailbackend.mail

import de.thm.mni.mailbackend.utils.currentTime
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Business logic for sending emails.
 *
 * It applies the status workflow of `domain.md`: `DRAFT -> SENT`, `DRAFT -> ERROR`,
 * `ERROR -> SENT`, `ERROR -> ERROR`. A sent email is never sent again (rule E6). Every attempt
 * is recorded as a [Transmission].
 *
 * @property emailRepository storage for emails and their send attempts
 * @property mailGateway connection to the mail server (a mock in this project)
 */
@Service
class TransmissionService(
    private val emailRepository: EmailRepository,
    private val mailGateway: MailTransmissionGateway,
) {

    private val log = LoggerFactory.getLogger(TransmissionService::class.java)

    /**
     * Returns all send attempts of an email, the newest first.
     *
     * @param emailId identifier of the email
     * @return the send attempts
     * @throws EmailNotFoundException if the email does not exist
     */
    fun list(emailId: UUID): List<Transmission> = findEmail(emailId).transmissions

    /**
     * Returns one send attempt of an email.
     *
     * @param emailId identifier of the email
     * @param transmissionId identifier of the send attempt
     * @return the send attempt
     * @throws EmailNotFoundException if the email does not exist
     * @throws TransmissionNotFoundException if the attempt does not belong to the email
     */
    fun get(emailId: UUID, transmissionId: UUID): Transmission =
        findEmail(emailId).transmissions.firstOrNull { it.id == transmissionId }
            ?: throw TransmissionNotFoundException(transmissionId)

    /**
     * Performs one send attempt and records it.
     *
     * A rejected request (sent or received email) changes nothing and records no attempt.
     * An accepted attempt that fails is not an error of this method: it is recorded with the
     * outcome [TransmissionOutcome.FAILED] and the email gets the status [EmailStatus.ERROR].
     *
     * Attempts are processed one after another, so an email can never be sent twice.
     *
     * @param emailId identifier of the email
     * @return the recorded send attempt
     * @throws EmailNotFoundException if the email does not exist
     * @throws EmailNotSendableException if the email was received from outside
     * @throws EmailReadOnlyException if the email was already sent (rule E6)
     */
    @Synchronized
    fun send(emailId: UUID): Transmission {
        val email = findEmail(emailId)
        if (email.metadata.source == EmailSource.EXTERN) {
            throw EmailNotSendableException(emailId)
        }
        if (email.isReadOnly) {
            throw EmailReadOnlyException(emailId)
        }

        val result = deliver(email)
        val now = currentTime()
        val transmission = Transmission(
            id = UUID.randomUUID(),
            emailId = emailId,
            outcome = if (result is DeliveryResult.Delivered) TransmissionOutcome.SUCCEEDED else TransmissionOutcome.FAILED,
            attemptedAt = now,
            failureReason = (result as? DeliveryResult.Rejected)?.reason,
        )
        val metadata = when (result) {
            DeliveryResult.Delivered -> email.metadata.copy(status = EmailStatus.SENT, sentAt = now, updatedAt = now)
            is DeliveryResult.Rejected -> email.metadata.copy(status = EmailStatus.ERROR, updatedAt = now)
        }

        emailRepository.save(
            email.copy(
                metadata = metadata,
                transmissions = listOf(transmission) + email.transmissions,
            ),
        )
        log.info("Send attempt {} for email {}: {}", transmission.id, emailId, transmission.outcome)
        return transmission
    }

    /**
     * Hands the email to the mail server. If the server cannot be reached, the attempt counts as
     * failed: the error is logged with all details, the user only gets a general reason.
     */
    private fun deliver(email: Email): DeliveryResult =
        try {
            mailGateway.send(email)
        } catch (ex: RuntimeException) {
            log.error("Mail server failed for email {}", email.id, ex)
            DeliveryResult.Rejected("The mail server could not be reached.")
        }

    private fun findEmail(emailId: UUID): Email =
        emailRepository.findById(emailId) ?: throw EmailNotFoundException(emailId)
}
