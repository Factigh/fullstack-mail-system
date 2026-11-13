package de.thm.mni.mailbackend.utils

/**
 * One page of a larger result.
 *
 * @param T type of the elements
 * @property items elements on this page
 * @property page zero-based number of this page
 * @property size requested number of elements per page
 * @property totalElements number of all matching elements on all pages
 */
data class PageResult<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
) {
    /** Number of pages, `0` if nothing matches. */
    val totalPages: Int
        get() = if (totalElements == 0L) 0 else ((totalElements + size - 1) / size).toInt()
}
