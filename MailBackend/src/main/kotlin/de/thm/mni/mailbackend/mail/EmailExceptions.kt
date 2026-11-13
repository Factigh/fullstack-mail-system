package de.thm.mni.mailbackend.mail

import de.thm.mni.mailbackend.framework.error.ConflictException
import de.thm.mni.mailbackend.framework.error.ContentTooLargeException
import de.thm.mni.mailbackend.framework.error.NotFoundException
import de.thm.mni.mailbackend.framework.error.RuleViolationException
import java.util.UUID

/**
 * Thrown when an email does not exist.
 *
 * @param id identifier of the requested email
 */
class EmailNotFoundException(id: UUID) : NotFoundException("The email $id does not exist.")

/**
 * Thrown when a sent email or its attachments should be changed (rule E6).
 *
 * @param id identifier of the email
 */
class EmailReadOnlyException(id: UUID) : ConflictException("The email $id was already sent and is read-only.")

/**
 * Thrown when an attachment does not exist or does not belong to the given email.
 *
 * @param id identifier of the requested attachment
 */
class AttachmentNotFoundException(id: UUID) : NotFoundException("The attachment $id does not exist.")

/**
 * Thrown when an uploaded file breaks an attachment rule other than a size limit,
 * for example the maximum number of attachments (rules F1, F3, F4).
 *
 * @param message description of the broken rule
 */
class AttachmentRuleViolationException(message: String) : RuleViolationException(message)

/**
 * Thrown when an uploaded file exceeds a size limit (rule F2).
 *
 * @param message description of the exceeded limit
 */
class AttachmentTooLargeException(message: String) : ContentTooLargeException(message)
