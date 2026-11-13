package de.thm.mni.mailbackend.mail.api

import de.thm.mni.mailbackend.mail.Attachment
import de.thm.mni.mailbackend.mail.AttachmentUpload
import org.springframework.web.multipart.MultipartFile

/*
 * Conversions between the API representation of attachments and the domain classes.
 */

/** Converts a domain attachment into its API metadata. The storage location is never exposed. */
fun Attachment.toMetadata(): AttachmentMetadata =
    AttachmentMetadata(
        id = id,
        fileName = fileName,
        mimeType = mimeType,
        size = size,
    )

/**
 * Converts a file from a `multipart/form-data` request into a domain upload.
 *
 * File name and MIME type come from the headers of the part, the size from Spring.
 * The content is only opened when the business layer stores the file.
 */
fun MultipartFile.toUpload(): AttachmentUpload =
    AttachmentUpload(
        fileName = originalFilename,
        mimeType = contentType,
        size = size,
        openStream = { inputStream },
    )
