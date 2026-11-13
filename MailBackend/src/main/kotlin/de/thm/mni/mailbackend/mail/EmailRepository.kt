package de.thm.mni.mailbackend.mail

import de.thm.mni.mailbackend.utils.PageResult
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Mock repository that keeps emails in memory instead of a database.
 *
 * The data is lost when the application stops. It will be replaced by a real database later;
 * the rest of the application only uses the public methods and does not notice the change.
 */
@Repository
class EmailRepository {

    private val emails = ConcurrentHashMap<UUID, Email>()

    /**
     * Stores a new email or replaces an existing email with the same id.
     *
     * @param email the email to store
     * @return the stored email
     */
    fun save(email: Email): Email {
        emails[email.id] = email
        return email
    }

    /**
     * Finds an email by its id.
     *
     * @param id identifier of the email
     * @return the email, or `null` if it does not exist
     */
    fun findById(id: UUID): Email? = emails[id]

    /**
     * Checks whether an email exists.
     *
     * @param id identifier of the email
     * @return `true` if the email exists
     */
    fun existsById(id: UUID): Boolean = emails.containsKey(id)

    /**
     * Removes an email. Nothing happens if it does not exist.
     *
     * @param id identifier of the email
     */
    fun deleteById(id: UUID) {
        emails.remove(id)
    }

    /**
     * Returns one page of emails, filtered and in a stable order: `createdAt` descending,
     * then `id` ascending. The filters are applied before the page is cut.
     *
     * @param status only emails with this status, or all if `null`
     * @param source only emails with this source, or all if `null`
     * @param page zero-based page number
     * @param size number of emails per page
     * @return the requested page
     */
    fun findPage(status: EmailStatus?, source: EmailSource?, page: Int, size: Int): PageResult<Email> {
        val matching = emails.values
            .filter { status == null || it.metadata.status == status }
            .filter { source == null || it.metadata.source == source }
            .sortedWith(compareByDescending<Email> { it.metadata.createdAt }.thenBy { it.id })

        val offset = page.toLong() * size
        val items = if (offset >= matching.size) emptyList() else matching.drop(offset.toInt()).take(size)
        return PageResult(items, page, size, matching.size.toLong())
    }
}
