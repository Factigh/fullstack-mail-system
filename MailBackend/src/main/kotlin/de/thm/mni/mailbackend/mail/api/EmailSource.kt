package de.thm.mni.mailbackend.mail.api

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Origin of an email as it appears in the API.
 *
 * The JSON values are lowercase (`intern`, `extern`), as defined in `openapi.yaml`.
 */
enum class EmailSource {
    /** The email was written in this system. */
    @JsonProperty("intern")
    INTERN,

    /** The email was received from outside. */
    @JsonProperty("extern")
    EXTERN,
}
