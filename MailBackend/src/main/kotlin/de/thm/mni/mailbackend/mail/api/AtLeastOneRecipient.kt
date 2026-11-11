package de.thm.mni.mailbackend.mail.api

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Validation rule: at least one address must exist in `to`, `cc`, or `bcc` (domain rule E1).
 *
 * The rule concerns three fields at once, so it is placed on the class instead of a single
 * field. The error is reported for the field `to`, as defined in `openapi.yaml`.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [AtLeastOneRecipientValidator::class])
annotation class AtLeastOneRecipient(
    val message: String = "At least one recipient is required in to, cc, or bcc.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Checks the [AtLeastOneRecipient] rule for every request body that implements [HasRecipients].
 */
class AtLeastOneRecipientValidator : ConstraintValidator<AtLeastOneRecipient, HasRecipients> {

    override fun isValid(value: HasRecipients?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            return true
        }
        val hasRecipient = value.to.isNotEmpty() || value.cc.isNotEmpty() || value.bcc.isNotEmpty()
        if (!hasRecipient) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate(context.defaultConstraintMessageTemplate)
                .addPropertyNode("to")
                .addConstraintViolation()
        }
        return hasRecipient
    }
}
