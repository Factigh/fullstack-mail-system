package de.thm.mni.mailbackend.mail

import de.thm.mni.mailbackend.utils.currentTime
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Business logic for attachments.
 *
 * It applies the attachment rules of `domain.md` (F1 to F6) and the rule that a sent email
 * is read-only (E6). The metadata is part of the email and is stored by the
 * [EmailRepository]; the bytes are stored by the [AttachmentFileStore].
 *
 * @property emailRepository storage for emails and their attachment metadata
 * @property fileStore storage for the bytes of the attachments
 */
@Service
class AttachmentService(
    private val emailRepository: EmailRepository,
    private val fileStore: AttachmentFileStore,
) {

    private val log = LoggerFactory.getLogger(AttachmentService::class.java)

    /**
     * Returns the metadata of all attachments of an email.
     *
     * @param emailId identifier of the email
     * @return the attachments
     * @throws EmailNotFoundException if the email does not exist
     */
    fun list(emailId: UUID): List<Attachment> = findEmail(emailId).attachments

    /**
     * Returns the metadata of one attachment of an email.
     *
     * @param emailId identifier of the email
     * @param attachmentId identifier of the attachment
     * @return the attachment
     * @throws EmailNotFoundException if the email does not exist
     * @throws AttachmentNotFoundException if the attachment does not belong to the email
     */
    fun get(emailId: UUID, attachmentId: UUID): Attachment =
        findAttachment(findEmail(emailId), attachmentId)

    /**
     * Checks an uploaded file against the attachment rules, stores it, and adds it to the email.
     *
     * @param emailId identifier of the email
     * @param upload the uploaded file
     * @return the metadata of the new attachment
     * @throws EmailNotFoundException if the email does not exist
     * @throws EmailReadOnlyException if the email was already sent (rule E6)
     * @throws AttachmentRuleViolationException if a rule other than a size limit is broken
     * @throws AttachmentTooLargeException if a size limit is exceeded (rule F2)
     */
    fun add(emailId: UUID, upload: AttachmentUpload): Attachment {
        val email = findEditableEmail(emailId)
        val fileName = upload.fileName?.trim().orEmpty()
        val mimeType = upload.mimeType?.trim().takeUnless { it.isNullOrEmpty() } ?: AttachmentRules.DEFAULT_MIME_TYPE

        when {
            email.attachments.size >= AttachmentRules.MAX_COUNT ->
                throw AttachmentRuleViolationException("An email has at most ${AttachmentRules.MAX_COUNT} attachments.")
            upload.size < 1 ->
                throw AttachmentRuleViolationException("The file must not be empty.")
            upload.size > AttachmentRules.MAX_FILE_SIZE ->
                throw AttachmentTooLargeException("The file is larger than ${AttachmentRules.MAX_FILE_SIZE} bytes.")
            email.totalAttachmentSize + upload.size > AttachmentRules.MAX_TOTAL_SIZE ->
                throw AttachmentTooLargeException(
                    "All attachments of an email together have at most ${AttachmentRules.MAX_TOTAL_SIZE} bytes.",
                )
            !AttachmentRules.isValidFileName(fileName) ->
                throw AttachmentRuleViolationException(
                    "The file name must have 1 to ${AttachmentRules.MAX_FILE_NAME_LENGTH} characters and no path separators.",
                )
            !AttachmentRules.isValidMimeType(mimeType) ->
                throw AttachmentRuleViolationException("The MIME type must have the form type/subtype.")
        }

        val attachment = Attachment(UUID.randomUUID(), fileName, mimeType, upload.size)
        fileStore.save(attachment.id, upload.openStream)
        emailRepository.save(
            email.copy(
                attachments = email.attachments + attachment,
                metadata = email.metadata.copy(updatedAt = currentTime()),
            ),
        )
        log.info("Added attachment {} ({} bytes) to email {}", attachment.id, attachment.size, emailId)
        return attachment
    }

    /**
     * Opens the content of an attachment for a download.
     *
     * @param emailId identifier of the email
     * @param attachmentId identifier of the attachment
     * @return metadata and opened content; the caller closes the stream
     * @throws EmailNotFoundException if the email does not exist
     * @throws AttachmentNotFoundException if the attachment does not belong to the email
     */
    fun openContent(emailId: UUID, attachmentId: UUID): AttachmentContent {
        val attachment = get(emailId, attachmentId)
        return AttachmentContent(attachment, fileStore.open(attachment.id))
    }

    /**
     * Deletes an attachment and its file.
     *
     * @param emailId identifier of the email
     * @param attachmentId identifier of the attachment
     * @throws EmailNotFoundException if the email does not exist
     * @throws EmailReadOnlyException if the email was already sent (rule E6)
     * @throws AttachmentNotFoundException if the attachment does not belong to the email
     */
    fun delete(emailId: UUID, attachmentId: UUID) {
        val email = findEditableEmail(emailId)
        val attachment = findAttachment(email, attachmentId)

        fileStore.delete(attachment.id)
        emailRepository.save(
            email.copy(
                attachments = email.attachments - attachment,
                metadata = email.metadata.copy(updatedAt = currentTime()),
            ),
        )
        log.info("Deleted attachment {} of email {}", attachmentId, emailId)
    }

    private fun findEmail(emailId: UUID): Email =
        emailRepository.findById(emailId) ?: throw EmailNotFoundException(emailId)

    private fun findEditableEmail(emailId: UUID): Email {
        val email = findEmail(emailId)
        if (email.isReadOnly) {
            throw EmailReadOnlyException(emailId)
        }
        return email
    }

    private fun findAttachment(email: Email, attachmentId: UUID): Attachment =
        email.attachments.firstOrNull { it.id == attachmentId } ?: throw AttachmentNotFoundException(attachmentId)
}
