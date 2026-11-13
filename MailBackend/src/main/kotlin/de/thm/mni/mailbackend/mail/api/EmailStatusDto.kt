package de.thm.mni.mailbackend.mail.api

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

/**
 * Status of an email as it appears in the API.
 *
 * The JSON values are lowercase (`draft`, `sent`, `error`), as defined in `openapi.yaml`.
 * The domain uses its own enum, so that the domain does not depend on JSON.
 */
enum class EmailStatusDto {
    /** The email was created but not sent yet. */
    @JsonProperty("draft")
    DRAFT,

    /** The email was sent successfully and is read-only. */
    @JsonProperty("sent")
    SENT,

    /** The last send attempt failed. The email can be edited and sent again. */
    @JsonProperty("error")
    ERROR,
    ;

    companion object {
        /**
         * Reads the API value of a status, for example from a query parameter.
         *
         * @param value lowercase API value, for example `draft`
         * @return the matching status
         * @throws IllegalArgumentException if the value is unknown
         */
        fun fromValue(value: String): EmailStatusDto =
            entries.firstOrNull { it.name.lowercase() == value }
                ?: throw IllegalArgumentException("Unknown email status: $value")
    }
}

/**
 * Lets Spring read query parameters such as `?status=draft` into [EmailStatusDto].
 *
 * Spring Boot registers every `Converter` bean automatically. An unknown value answers `400`.
 */
@Component
class EmailStatusDtoConverter : Converter<String, EmailStatusDto> {
    override fun convert(source: String): EmailStatusDto = EmailStatusDto.fromValue(source)
}
