package de.thm.mni.mailbackend.mail.api

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Status of an email as it appears in the API.
 *
 * The JSON values are lowercase (`draft`, `sent`, `error`), as defined in `openapi.yaml`.
 */
enum class EmailStatus {
    /** The email was created but not sent yet. */
    @JsonProperty("draft")
    DRAFT,

    /** The email was sent successfully and is read-only. */
    @JsonProperty("sent")
    SENT,

    /** The last send attempt failed. The email can be edited and sent again. */
    @JsonProperty("error")
    ERROR,
}
