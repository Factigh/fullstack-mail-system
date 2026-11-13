package de.thm.mni.mailbackend.mail.api

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

/**
 * Origin of an email as it appears in the API.
 *
 * The JSON values are lowercase (`intern`, `extern`), as defined in `openapi.yaml`.
 * The domain uses its own enum, so that the domain does not depend on JSON.
 */
enum class EmailSourceDto {
    /** The email was written in this system. */
    @JsonProperty("intern")
    INTERN,

    /** The email was received from outside. */
    @JsonProperty("extern")
    EXTERN,
    ;

    companion object {
        /**
         * Reads the API value of a source, for example from a query parameter.
         *
         * @param value lowercase API value, for example `intern`
         * @return the matching source
         * @throws IllegalArgumentException if the value is unknown
         */
        fun fromValue(value: String): EmailSourceDto =
            entries.firstOrNull { it.name.lowercase() == value }
                ?: throw IllegalArgumentException("Unknown email source: $value")
    }
}

/**
 * Lets Spring read query parameters such as `?source=extern` into [EmailSourceDto].
 *
 * Spring Boot registers every `Converter` bean automatically. An unknown value answers `400`.
 */
@Component
class EmailSourceDtoConverter : Converter<String, EmailSourceDto> {
    override fun convert(source: String): EmailSourceDto = EmailSourceDto.fromValue(source)
}
