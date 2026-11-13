package de.thm.mni.mailbackend.mail

import de.thm.mni.mailbackend.utils.PageResult
import de.thm.mni.mailbackend.utils.currentTime
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Business logic for emails.
 *
 * This class applies the rules of `domain.md`. It knows nothing about HTTP or JSON and does not
 * know how emails are stored; it only uses the repository layer.
 *
 * @property emailRepository storage for emails
 * @property attachmentFileStore storage for attachment files, needed to delete them with the email
 */
@Service
class EmailService(
    private val emailRepository: EmailRepository,
    private val attachmentFileStore: AttachmentFileStore,
) {

    private val log = LoggerFactory.getLogger(EmailService::class.java)

    /**
     * Creates a new email with the status [EmailStatus.DRAFT] and the source [EmailSource.INTERN].
     *
     * @param message writable part of the new email
     * @return the stored email
     * @throws EmailRuleViolationException if the email has too many recipients (rule E2)
     */
    fun create(message: EmailMessage): Email {
        checkRecipientLimit(message)
        val now = currentTime()
        val email = Email(
            id = UUID.randomUUID(),
            message = message,
            metadata = EmailMetadata(
                createdAt = now,
                updatedAt = now,
                sentAt = null,
                status = EmailStatus.DRAFT,
                source = EmailSource.INTERN,
            ),
        )
        emailRepository.save(email)
        log.info("Created email {}", email.id)
        return email
    }

    /**
     * Returns one email.
     *
     * @param id identifier of the email
     * @return the email
     * @throws EmailNotFoundException if the email does not exist
     */
    fun get(id: UUID): Email =
        emailRepository.findById(id) ?: throw EmailNotFoundException(id)

    /**
     * Returns one page of emails.
     *
     * @param status only emails with this status, or all if `null`
     * @param source only emails with this source, or all if `null`
     * @param page zero-based page number
     * @param size number of emails per page
     * @return the requested page
     */
    fun list(status: EmailStatus?, source: EmailSource?, page: Int, size: Int): PageResult<Email> =
        emailRepository.findPage(status, source, page, size)

    /**
     * Replaces the writable part of an email and sets `updatedAt` to the current time.
     * The attachments stay unchanged.
     *
     * @param id identifier of the email
     * @param message new writable part of the email
     * @return the updated email
     * @throws EmailNotFoundException if the email does not exist
     * @throws EmailReadOnlyException if the email was already sent (rule E6)
     * @throws EmailRuleViolationException if the email has too many recipients (rule E2)
     */
    fun update(id: UUID, message: EmailMessage): Email {
        val email = get(id)
        if (email.isReadOnly) {
            throw EmailReadOnlyException(id)
        }
        checkRecipientLimit(message)
        val updated = email.copy(
            message = message,
            metadata = email.metadata.copy(updatedAt = currentTime()),
        )
        emailRepository.save(updated)
        log.info("Updated email {}", id)
        return updated
    }

    /**
     * Deletes an email permanently together with all its attachment files (rule F6),
     * regardless of its status.
     *
     * @param id identifier of the email
     * @throws EmailNotFoundException if the email does not exist
     */
    fun delete(id: UUID) {
        val email = get(id)
        email.attachments.forEach { attachmentFileStore.delete(it.id) }
        emailRepository.deleteById(id)
        log.info("Deleted email {} with {} attachment(s)", id, email.attachments.size)
    }

    private fun checkRecipientLimit(message: EmailMessage) {
        if (message.recipientCount > EmailMessage.MAX_RECIPIENTS) {
            throw EmailRuleViolationException(
                "to, cc, and bcc together have at most ${EmailMessage.MAX_RECIPIENTS} addresses.",
            )
        }
    }
}
