package de.thm.mni.mailbackend.mail

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

/**
 * Stores the bytes of attachments as files in a configurable folder
 * (`mail.attachments.directory` in `application.yaml`).
 *
 * Each file is stored under the id of its attachment, never under the name sent by the
 * client. A client therefore cannot choose where a file is written (no path traversal).
 *
 * @param directory folder for the files; a relative path is resolved against the working
 * directory of the application
 */
@Repository
class AttachmentFileStore(@Value("\${mail.attachments.directory}") directory: String) {

    private val log = LoggerFactory.getLogger(AttachmentFileStore::class.java)

    private val root: Path = Path.of(directory).toAbsolutePath().normalize()

    init {
        Files.createDirectories(root)
        log.info("Attachment files are stored in {}", root)
    }

    /**
     * Writes the content of a new attachment. The stream is closed afterwards.
     *
     * @param id identifier of the attachment
     * @param openStream opens the content to write
     */
    fun save(id: UUID, openStream: () -> InputStream) {
        openStream().use { input -> Files.copy(input, pathOf(id)) }
    }

    /**
     * Opens the content of an attachment for reading. The caller closes the stream.
     *
     * @param id identifier of the attachment
     * @return the content of the file
     */
    fun open(id: UUID): InputStream = Files.newInputStream(pathOf(id))

    /**
     * Deletes the file of an attachment. Nothing happens if it does not exist.
     *
     * @param id identifier of the attachment
     */
    fun delete(id: UUID) {
        Files.deleteIfExists(pathOf(id))
    }

    private fun pathOf(id: UUID): Path = root.resolve(id.toString())
}
