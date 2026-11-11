package de.thm.mni.mailbackend.mail.api

import de.thm.mni.mailbackend.mail.EmailAddressRules
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Validation rule for a list of email addresses:
 * every entry is a valid address (rules A1 and A2) and no address appears twice,
 * ignoring upper and lower case (rule E3).
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [ValidAddressListValidator::class])
annotation class ValidAddressList(
    val message: String = "Contains an invalid email address.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Checks the [ValidAddressList] rule.
 *
 * The error messages are fixed texts. They never contain the user's input, because
 * message templates are interpreted by the validation library.
 */
class ValidAddressListValidator : ConstraintValidator<ValidAddressList, List<String>> {

    override fun isValid(value: List<String>?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            return true
        }
        val message = when {
            value.any { !EmailAddressRules.isValid(it) } -> INVALID_ADDRESS
            value.map { it.lowercase() }.toSet().size != value.size -> DUPLICATE_ADDRESS
            else -> return true
        }
        context.disableDefaultConstraintViolation()
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation()
        return false
    }

    private companion object {
        const val INVALID_ADDRESS = "Contains an invalid email address."
        const val DUPLICATE_ADDRESS = "Contains the same address more than once."
    }
}
