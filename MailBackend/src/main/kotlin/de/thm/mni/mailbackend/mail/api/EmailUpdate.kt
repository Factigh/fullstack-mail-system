package de.thm.mni.mailbackend.mail.api

/**
 * Request body to replace the writable data of an email (`PUT /api/v1/emails/{emailId}`).
 *
 * It has exactly the same fields and validation rules as [EmailCreate], as defined in
 * `openapi.yaml`. An update can therefore never produce an email without recipients.
 * A type alias avoids writing the class and its rules twice.
 */
typealias EmailUpdate = EmailCreate
