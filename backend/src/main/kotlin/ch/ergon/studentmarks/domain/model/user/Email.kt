package ch.ergon.studentmarks.domain.model.user

@ConsistentCopyVisibility
data class Email private constructor(
    val value: String,
) {
    companion object {
        // TODO: Add validation
        fun create(value: String) = Email(value.lowercase())
    }
}
