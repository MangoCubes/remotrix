package ch.skew.remotrix.classes

class InvalidPhoneNumber: Throwable()

class PhoneNumber(val number: String) {

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PhoneNumber) return false
        return other.number == this.number
    }

    override fun toString(): String {
        return this.number
    }

    companion object {
        fun from(number: String): Result<PhoneNumber>{
            val filtered = number.filter { it.isDigit() }
            return if(filtered.isEmpty()) Result.failure(InvalidPhoneNumber())
            else Result.success(PhoneNumber(filtered))
        }
    }
}