import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kvalidator.*

class TestValidator {
    data class Apple(val id: Int, val weight: Double, val type: String) {
        init {
            validator {
                validate("id", "Id must be positive") { id > 0}
                validate("weight", "weight must be between 1 and 25 unless id is 0, then it must be 50")
                {
                    if (id == 0) { weight == 50.0 } else { weight in 1.0 .. 25.0 }
                }
                validate("type", "Invalid type", type) {
                    type in listOf("Cortland", "Granny Smith", "Red Delicious", "Green Delicious")
                }
            }
        }
    }

    @Test
    fun testValid() {
        Apple(1, 10.3, "Cortland")
    }

    @Test
    fun testInvalidThrowsException() {
        val exception = assertFailsWith<ValidationException>() {
            Apple(-1, 50.0, "Granny Green")
        }
        assertEquals(3, exception.failures.size)
        assertEquals("Id must be positive", exception.failures["id"]?.message)
        assertEquals("Granny Green", exception.failures["type"]?.value)
    }

    @Test
    fun testInvalidWithResult() {
        var a = -1
        var b = 9
        resultValidator {
            validate("a", "A must be positive") { a > 0}
            validate("b", "B must be a positive multiple of 3")
            { (b > 0) && (b % 3 == 0)}
        }.recoverCatching {
            val e = it as ValidationException
            if ("a" in e.failures) {
                a = 0
            }
            if ("b" in e.failures) {
                b = 3
            }
        }
        assertEquals(0, a)
        assertEquals(9, b)
    }
}