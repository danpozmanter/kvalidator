package kvalidator

/**
 * Hold a validation failure in a mapping of failures indexed by property/variable name.
 *
 * @property message Error message for failed validation of the property/variable.
 * @property value Optional. Value of the property/variable.
 */
data class ValidationFailure(val message: String, val value: Any? = null)

/**
 * Exception thrown when validation through a Validator has failed.
 *
 * @property message General error message for failed overall validation.
 * @property failures Mapping of property/variable name to ValidationFailure(message, value?).
 */
data class ValidationException(override val message: String, val failures: Map<String, ValidationFailure>): Exception(message)

/**
 * Run validate functions, accumulate any failures.
 *
 * @property failures a map of ValidationFailures by property name.
 */
class Validator(var failures: MutableMap<String, ValidationFailure> = mutableMapOf()) {
    /**
     * Run the provided `check` function to determine if validation passes.
     * If not, add a ValidationFailure (indexed by property) with an error message and optional value.
     *
     * @param name The name of the property or variable being validated. Index for the failures map.
     * @param message Error message if validation fails.
     * @param value Optional. Value of the property/variable.
     * @param check A function literal that returns a Boolean. Used to determine if the property/value is valid.
     */
    inline fun validate(name: String, message: String, value: Any? = null, check: () -> Boolean) {
        if (!check()) {
            this.failures[name] = ValidationFailure(message, value)
        }
    }
}

/**
 * Function literal with receiver.
 * Instantiates a `Validator` instance.
 * Runs all `validate` calls, adding to `failures` when validation fails.
 * If `failures` exist, throws a `ValidationException` with those failures included.
 */
inline fun validator(init: Validator.() -> Unit): Validator {
    val v = Validator()
    v.init()
    if (v.failures.isNotEmpty()) {
        throw ValidationException("Validation failed", v.failures)
    }
    return v
}

/**
 * Function literal with receiver.
 * Instantiates a `Validator` instance.
 * Runs all `validate` calls, adding to `failures` when validation fails.
 * Returns a `Result`, with success if no `failures` exist, otherwise
 * a failure with a `ValidationException` as the throwable.
 */
inline fun resultValidator(init: Validator.() -> Unit): Result<Validator> = runCatching {
    val v = Validator()
    v.init()
    if (v.failures.isNotEmpty()) {
        throw ValidationException("Validation failed", v.failures)
    }
    v
}