package de.thm.mni.mailbackend.mail

/**
 * Limits and rules for attachments (domain rules F1 to F4 in `domain.md`).
 */
object AttachmentRules {

    /** An email has at most 10 attachments (rule F1). */
    const val MAX_COUNT = 10

    /** A single file has at most 10 MB (rule F2). */
    const val MAX_FILE_SIZE: Long = 10L * 1024 * 1024

    /** All attachments of one email together have at most 25 MB (rule F2). */
    const val MAX_TOTAL_SIZE: Long = 25L * 1024 * 1024

    /** A file name has at most 255 characters (rule F3). */
    const val MAX_FILE_NAME_LENGTH = 255

    /** MIME type used when the client sends none. */
    const val DEFAULT_MIME_TYPE = "application/octet-stream"

    private val mimeTypeRegex = Regex("^[A-Za-z0-9!#$&^_.+-]+/[A-Za-z0-9!#$&^_.+-]+$")

    /**
     * Checks rule F3: 1 to 255 characters and no path separators.
     *
     * @param fileName the file name to check
     * @return `true` if the file name is valid
     */
    fun isValidFileName(fileName: String): Boolean =
        fileName.isNotBlank() &&
            fileName.length <= MAX_FILE_NAME_LENGTH &&
            !fileName.contains('/') &&
            !fileName.contains('\\')

    /**
     * Checks rule F4: the MIME type has the form `type/subtype`.
     *
     * @param mimeType the MIME type to check
     * @return `true` if the MIME type is valid
     */
    fun isValidMimeType(mimeType: String): Boolean = mimeTypeRegex.matches(mimeType)
}
