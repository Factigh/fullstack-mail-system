package de.thm.mni.mailbackend.mail

import de.thm.mni.mailbackend.framework.error.ConflictException
import de.thm.mni.mailbackend.framework.error.NotFoundException
import java.util.UUID

/**
 * Thrown when an email does not exist.
 *
 * @param id identifier of the requested email
 */
class EmailNotFoundException(id: UUID) : NotFoundException("The email $id does not exist.")

/**
 * Thrown when a sent email should be changed (rule E6).
 *
 * @param id identifier of the email
 */
class EmailReadOnlyException(id: UUID) : ConflictException("The email $id was already sent and is read-only.")
