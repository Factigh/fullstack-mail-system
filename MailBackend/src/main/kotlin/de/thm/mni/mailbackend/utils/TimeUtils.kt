package de.thm.mni.mailbackend.utils

import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

/**
 * Returns the current time in the server's time zone, truncated to whole seconds.
 *
 * All timestamps of the application use this function, so they have the same precision.
 */
fun currentTime(): OffsetDateTime = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS)
