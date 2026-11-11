package de.thm.mni.mailbackend.mail

/**
 * Rules for a syntactically valid email address (domain rules A1 and A2 in `domain.md`).
 *
 * The constants are `const` so that they can be used inside validation annotations.
 */
object EmailAddressRules {

    /** An address has the form `local@domain` and contains no whitespace (rule A1). */
    const val PATTERN = "^[^\\s@]+@[^\\s@]+$"

    /** An address has at most 254 characters (rule A2). */
    const val MAX_LENGTH = 254

    private val regex = Regex(PATTERN)

    /**
     * Checks whether the given text is a syntactically valid email address.
     *
     * @param address the text to check
     * @return `true` if the address follows rules A1 and A2
     */
    fun isValid(address: String): Boolean =
        address.length <= MAX_LENGTH && regex.matches(address)
}
