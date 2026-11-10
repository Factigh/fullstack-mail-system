package de.thm.mni.mailbackend.mail.api

import java.util.UUID

/**
 * Metadata of an attachment as it appears in API responses.
 *
 * The storage location of the file is never part of the response.
 *
 * @property id identifier of the attachment
 * @property fileName original name of the uploaded file
 * @property mimeType MIME type of the file, for example `application/pdf`
 * @property size size of the file in bytes
 */
data class AttachmentMetadata(
    val id: UUID,
    val fileName: String,
    val mimeType: String,
    val size: Long,
)
