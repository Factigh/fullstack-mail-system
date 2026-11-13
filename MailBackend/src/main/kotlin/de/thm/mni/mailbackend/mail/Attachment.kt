package de.thm.mni.mailbackend.mail

import java.io.InputStream
import java.util.UUID

/**
 * Metadata of a file attached to exactly one email (see `domain.md`).
 *
 * The bytes of the file are not part of this class. They are kept by the
 * [AttachmentFileStore] under the id of the attachment.
 *
 * @property id identifier of the attachment
 * @property fileName original name of the uploaded file
 * @property mimeType MIME type of the file, for example `application/pdf`
 * @property size size of the file in bytes
 */
data class Attachment(
    val id: UUID,
    val fileName: String,
    val mimeType: String,
    val size: Long,
)

/**
 * A file that a user wants to attach, before it was checked and stored.
 *
 * The content is read lazily through [openStream], so a rejected file is never read.
 *
 * @property fileName file name sent by the client, may be missing
 * @property mimeType MIME type sent by the client, may be missing
 * @property size size of the file in bytes
 * @property openStream opens the content of the file; the caller closes the stream
 */
class AttachmentUpload(
    val fileName: String?,
    val mimeType: String?,
    val size: Long,
    val openStream: () -> InputStream,
)

/**
 * The metadata and the opened content of a stored attachment, for a download.
 *
 * @property attachment metadata of the attachment
 * @property stream content of the file; the receiver closes it
 */
class AttachmentContent(
    val attachment: Attachment,
    val stream: InputStream,
)
