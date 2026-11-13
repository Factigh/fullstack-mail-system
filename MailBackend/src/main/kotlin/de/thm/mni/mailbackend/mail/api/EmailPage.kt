package de.thm.mni.mailbackend.mail.api

/**
 * Response body with one page of email summaries (`GET /api/v1/emails`).
 *
 * @property data email summaries on this page
 * @property page zero-based number of this page
 * @property size requested number of elements per page
 * @property totalElements number of emails that match the filters
 * @property totalPages number of pages, `0` if nothing matches
 */
data class EmailPage(
    val data: List<EmailSummary>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
