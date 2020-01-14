# Request Validation

For every request that the connector receives, there should be a JSON-schema defined with the required fields, field types, possible lengths, regular expressions to match, etc.

By validating early in the process, we avoid calling the PSP with a request that we know is not going to be successful, reducing times and costs. Of course, the PSP is going to perform much more validations on the operation, which will be indicated with a successful response (no exception is thrown) in the `OperationResponse` object with the `status` field in the corresponding value (maybe an invalid card, invalid amount, etc.). 

For more information about JSON-schema, you can check [this guide](https://json-schema.org/learn/getting-started-step-by-step.html).

## Validation Classes

```
com.deliveryhero.alfred.connector.sdk.operation.request.validation
├── Validation
├── ValidationDetail
└── ValidationResult
```

### `Validation`

This class groups the information about the result of a request validation.

`result: ValidationResult`

The final result of the validation.

`errors: List<ValidationDetail>`

In case the validation is successful (`result = OK`), this field will be `null`. However, if any violations were found in the request, they will be listed here. Each `ValidationDetail` item contains the details about the field and the problem found.

---

### `ValidationResult`

This enum indicates the result of the validation. 

The possible values are:

- `OK`: No violations found during request validation.
- `ERROR`: The validation process found violations to the defined schema, the details should be in the `errors` field. 
- `NO_SCHEMA`: There is no JSON-schema defined for the request, so there is no way we can validate it.

---

### `ValidationDetail`

This class contains the detailed information of a failed validation of a field while comparing with the defined schema for the request.

----

`field: String`

The name of the field. The full name is composed of the dot-separated levels starting from the `OperationRequest` object. For example, if the email inside the `Customer` object fails, it will be `customer.email`.

----

`errorCode: String`

Developer-oriented error code that can be used in a switch block to classify the error. For example: `invalid.email.format`.

----

`error: String`

Full description of the error that can be logged and used for correcting the failing validation.
