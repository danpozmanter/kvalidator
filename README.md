# KValidator

Inspired by [Pydantic](https://github.com/samuelcolvin/pydantic) `@validator` for Python.

Validate simply without reflection or a complex DSL.

## Using KValidator

### Throw an Exception

This approach will capture all validation failures, and throw a ValidationException on failure.

```kotlin
data class Apple(val id: Int, val weight: Double, val type: String) {
    init {
        validator {
            validate("id", "Id must be positive") { id > 0}
            validate("weight", "Weight must be between 1 and 25 unless id is 1 then it must be 50") 
            {
                if (id == 1) { // You can check the values of multiple fields in your validation
                    weight == 50.0
                } else {
                    weight in 1.0 .. 25.0
                } 
            }
            validate("type", "Invalid type", type) {
                type in listOf("Cortland", "Granny Smith", "Red Delicious", "Green Delicious")
            }
        }
    }
}
```

Create a `validator` block, and call the `validate` function, passing in the property name, an error message
on validation failure, (the value of the property - optional), and a lambda that returns a Boolean.

If any validations fail, they will be collected as ValidationFailure instances in a Map, and accessible from the ValidationException as `failures`,
indexed by property name.

### Return a Result

This approach will capture all validation failures, and return a [Result](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/).

```kotlin
fun buildApple(id: Int, weight: Double, type: String): Apple {
    resultValidator {
        validate("id", "Id must be positive") { id > 0}
        validate("weight", "Weight must be between 1 and 25") { weight in 1.0 .. 25.0 }
        validate("type", "Invalid type", type) {
            type in listOf("Cortland", "Granny Smith", "Red Delicious", "Green Delicious")
        }
    }.fold(
        {
            return Apple(id, weight, type)
        }, // OnSuccess
        {
            val except = it as ValidationException
            if ("type" in except.failures) {
                raise SomeException("How Dare?!")
            }
            if ("id" in except.failures) {
                id = generateNextId()
            }
            if ("weight" in except.failures) {
                weight = getDefaultWeight()
            }
            return Apple(id, weight, type)
        }  // OnFailure
    )
}
```


## Other Approaches

Other approaches to validation in Kotlin:
* [Valiktor](https://github.com/valiktor/valiktor) (Uses a Fluent DSL)
* [Konform](https://www.konform.io/) (Uses a DSL)
* Use [require](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/require.html)
* Manual Approach

### Require

This approach will fail on the first error with an IllegalArgumentException.

```kotlin
data class Apple(val id: Int, val weight: Double, val type: String) {
    init {
        require(id > 0, "Id must be positive")
        require(weight in 1.0 .. 25.0, "Weight must be between 1 and 25")
        require(type in listOf("Cortland", "Granny Smith", "Red Delicious", "Green Delicious"), "Invalid type")
    }
}
```

### Manual Approach 

This approach ensures all validation errors are caught.

```kotlin
data class Apple(val id: Int, val weight: Double, val type: String) {
    init {
        var failures = mutableMapOf<KValidationError>()
        if (id <= 0) {
            failures.put("id", "Id must be positive")
        }
        if (weight !in 1.0 .. 25.0) {
            failures.put("weight", "Weight must be between 1 and 25")
        }
        if (type !in listOf("Cortland", "Granny Smith", "Red Delicious", "Green Delicious")) {
            failures.put("type", "Invalid type: $type")
        }
        if (failures.size > 0) {
            throw KValidationException("Validation failed", failures)
        }
    }
}
```

### Pydantic for Reference

```python
class Apple(BaseModel):
    id: int
    weight: float
    type: str

    @validator('id')
    def valid_id(cls, v: int):
        assert v >= 0, "ID must be positive"
        return v

    @validator('weight')
    def valid_weight(cls, v: float):
        assert (v >= 0.0 and v < 25.0), "Weight must be between 1 and 25"
        return v

    @validator('type')
    def valid_type(cls, v: str):
        assert v in ("Cortland", "Granny Smith", "Red Delicious", "Green Delicious"), "Invalid type"
        return v
```

