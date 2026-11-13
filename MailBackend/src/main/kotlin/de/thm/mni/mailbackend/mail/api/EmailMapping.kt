package de.thm.mni.mailbackend.mail.api

import de.thm.mni.mailbackend.mail.Email
import de.thm.mni.mailbackend.mail.EmailMessage
import de.thm.mni.mailbackend.mail.EmailSource
import de.thm.mni.mailbackend.mail.EmailStatus
import de.thm.mni.mailbackend.utils.PageResult

/*
 * Conversions between API DTOs and domain classes.
 *
 * They are extension functions, as shown in the lecture (slides 88 and 89): each conversion is
 * written once and called like a method, for example `email.toDetail()`.
 */

/**
 * Converts a create or update request into the writable part of a domain email.
 *
 * @param defaultFrom sender address used if the request has no `from`
 * @return the writable part of the email
 */
fun EmailCreate.toMessage(defaultFrom: String): EmailMessage =
    EmailMessage(
        from = from ?: defaultFrom,
        to = to,
        cc = cc,
        bcc = bcc,
        replyTo = replyTo,
        subject = subject,
        content = content,
    )

/**
 * Converts a domain email into the detailed API representation.
 *
 * Attachments are not implemented yet, so the list is always empty.
 */
fun Email.toDetail(): EmailDetail =
    EmailDetail(
        id = id,
        from = message.from,
        to = message.to,
        cc = message.cc,
        bcc = message.bcc,
        replyTo = message.replyTo,
        subject = message.subject,
        content = message.content,
        status = metadata.status.toDto(),
        source = metadata.source.toDto(),
        createdAt = metadata.createdAt,
        updatedAt = metadata.updatedAt,
        sentAt = metadata.sentAt,
        attachments = emptyList(),
    )

/**
 * Converts a domain email into the short API representation for lists.
 *
 * Attachments are not implemented yet, so the count is always `0`.
 */
fun Email.toSummary(): EmailSummary =
    EmailSummary(
        id = id,
        from = message.from,
        subject = message.subject,
        status = metadata.status.toDto(),
        source = metadata.source.toDto(),
        createdAt = metadata.createdAt,
        updatedAt = metadata.updatedAt,
        sentAt = metadata.sentAt,
        attachmentCount = 0,
    )

/** Converts a page of domain emails into the API page representation. */
fun PageResult<Email>.toEmailPage(): EmailPage =
    EmailPage(
        data = items.map { it.toSummary() },
        page = page,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
    )

/** Converts a domain status into its API representation. */
fun EmailStatus.toDto(): EmailStatusDto =
    when (this) {
        EmailStatus.DRAFT -> EmailStatusDto.DRAFT
        EmailStatus.SENT -> EmailStatusDto.SENT
        EmailStatus.ERROR -> EmailStatusDto.ERROR
    }

/** Converts an API status into its domain representation. */
fun EmailStatusDto.toDomain(): EmailStatus =
    when (this) {
        EmailStatusDto.DRAFT -> EmailStatus.DRAFT
        EmailStatusDto.SENT -> EmailStatus.SENT
        EmailStatusDto.ERROR -> EmailStatus.ERROR
    }

/** Converts a domain source into its API representation. */
fun EmailSource.toDto(): EmailSourceDto =
    when (this) {
        EmailSource.INTERN -> EmailSourceDto.INTERN
        EmailSource.EXTERN -> EmailSourceDto.EXTERN
    }

/** Converts an API source into its domain representation. */
fun EmailSourceDto.toDomain(): EmailSource =
    when (this) {
        EmailSourceDto.INTERN -> EmailSource.INTERN
        EmailSourceDto.EXTERN -> EmailSource.EXTERN
    }
