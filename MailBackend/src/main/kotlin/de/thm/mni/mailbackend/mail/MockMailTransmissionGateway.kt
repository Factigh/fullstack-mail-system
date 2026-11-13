package de.thm.mni.mailbackend.mail

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Mock of the external mail server. No email leaves the system.
 *
 * The behavior is predictable, so that both outcomes can be tested:
 * an email is rejected if one of its recipients (`to`, `cc`, `bcc`) has an address that ends
 * with `.invalid`, for example `someone@example.invalid`. The top-level domain `.invalid` is
 * reserved and never exists (RFC 2606). Every other email is accepted.
 */
@Component
class MockMailTransmissionGateway : MailTransmissionGateway {

    private val log = LoggerFactory.getLogger(MockMailTransmissionGateway::class.java)

    override fun send(email: Email): DeliveryResult {
        val recipients = email.message.to + email.message.cc + email.message.bcc
        val rejected = recipients.firstOrNull { it.lowercase().endsWith(INVALID_DOMAIN) }

        return if (rejected != null) {
            log.info("Mock mail server rejected email {}", email.id)
            DeliveryResult.Rejected("The mail server rejected the recipient $rejected.")
        } else {
            log.info("Mock mail server delivered email {} to {} recipient(s)", email.id, recipients.size)
            DeliveryResult.Delivered
        }
    }

    private companion object {
        const val INVALID_DOMAIN = ".invalid"
    }
}
