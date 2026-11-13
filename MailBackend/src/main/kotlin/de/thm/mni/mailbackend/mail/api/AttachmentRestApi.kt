package de.thm.mni.mailbackend.mail.api

import de.thm.mni.mailbackend.mail.AttachmentService
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * REST API for the attachments of one email (`/api/v1/emails/{emailId}/attachments`).
 *
 * The metadata of an attachment (`.../{attachmentId}`) and its bytes (`.../{attachmentId}/content`)
 * are two different resources, as defined in `openapi.yaml`.
 *
 * @property attachmentService business logic for attachments, injected by Spring
 */
@RestController
@RequestMapping("/api/v1/emails/{emailId}/attachments")
class AttachmentRestApi(private val attachmentService: AttachmentService) {

    /**
     * Lists the metadata of all attachments of an email.
     *
     * @param emailId identifier of the email
     * @return the attachment metadata; an unknown email answers `404`
     */
    @GetMapping
    fun listAttachments(@PathVariable emailId: UUID): List<AttachmentMetadata> =
        attachmentService.list(emailId).map { it.toMetadata() }

    /**
     * Uploads one file as `multipart/form-data` in the part named `file`.
     *
     * @param emailId identifier of the email
     * @param file the uploaded file
     * @return `201 Created` with the metadata and a `Location` header; a sent email answers `409`,
     * a broken rule `422`, a file that is too large `413`
     */
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadAttachment(
        @PathVariable emailId: UUID,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<AttachmentMetadata> {
        val attachment = attachmentService.add(emailId, file.toUpload())
        return ResponseEntity
            .created(URI.create("/api/v1/emails/$emailId/attachments/${attachment.id}"))
            .body(attachment.toMetadata())
    }

    /**
     * Returns the metadata of one attachment.
     *
     * @param emailId identifier of the email
     * @param attachmentId identifier of the attachment
     * @return the metadata; an attachment of another email answers `404`
     */
    @GetMapping("/{attachmentId}")
    fun getAttachmentMetadata(
        @PathVariable emailId: UUID,
        @PathVariable attachmentId: UUID,
    ): AttachmentMetadata =
        attachmentService.get(emailId, attachmentId).toMetadata()

    /**
     * Downloads the bytes of one attachment. The headers carry the MIME type, the file name,
     * and the size.
     *
     * @param emailId identifier of the email
     * @param attachmentId identifier of the attachment
     * @return the file content
     */
    @GetMapping("/{attachmentId}/content")
    fun downloadAttachmentContent(
        @PathVariable emailId: UUID,
        @PathVariable attachmentId: UUID,
    ): ResponseEntity<InputStreamResource> {
        val content = attachmentService.openContent(emailId, attachmentId)
        val attachment = content.attachment
        val disposition = ContentDisposition.attachment()
            .filename(attachment.fileName, StandardCharsets.UTF_8)
            .build()

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(attachment.mimeType))
            .contentLength(attachment.size)
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .body(InputStreamResource(content.stream))
    }

    /**
     * Deletes one attachment and its file.
     *
     * @param emailId identifier of the email
     * @param attachmentId identifier of the attachment
     */
    @DeleteMapping("/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAttachment(
        @PathVariable emailId: UUID,
        @PathVariable attachmentId: UUID,
    ) {
        attachmentService.delete(emailId, attachmentId)
    }
}
