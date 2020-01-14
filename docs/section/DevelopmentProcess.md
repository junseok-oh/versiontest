# Development Process

## Connector SDK

The Connector SDK is developed and maintained by the Payments team inside Alfred. To create it, Alfred's core service was considered to reduce as much as possible the impact and also we took some already known PSP integrations as a base to identify a common group of features, properties, naming, etc.

Like every piece of software that is used by someone, it is always being adapted to the new needs while keeping the compatibility for previous versions. This means that if a new PSP integration needs us to add a new field, for example, we are free to add it, but we should be careful of not introducing breaking changes to the rest of the users of the SDK (Alfred and other PSP integrations). That's why we must define a strong versioning strategy (described in the next section).

To get more information about how to contribute to the SDK, check the [Contributing Guide](../CONTRIBUTING.md).

## PSP Connector

For the specific PSP Connectors, the development and maintenance should be decided case by case. In some cases, it will be the Global Payments Team, for others the local entity Payments Team, some other third-party teams, or even external companies. The advantage of having the Connector SDK as a layer between Alfred and the connectors is that the developers only need access to this documentation. As long as the development complies with the request and response interface defined in the SDK, the developers don't need to worry about how Alfred is going to use it.

For more information about how to contribute to existing PSP Connectors, check the [Contributing Guide](../CONTRIBUTING.md).

For more information about how to start with a new PSP Connector, check the [Getting Started](GettingStarted.md) section.

## Versioning

We use Semantic Versioning as our strategy for versioning both the SDK and the PSP Connectors. You can read all about it [here](https://semver.org/).

As a summary:

    Given a version number MAJOR.MINOR.PATCH, increment the:
        MAJOR version when you make incompatible API changes,
        MINOR version when you add functionality in a backward-compatible manner, and
        PATCH version when you make backward-compatible bug fixes.

As an important note, keep in mind that during the development, you can keep the `0.y.z` format as long as you like. But as soon as the module is being used in production, the version should move to `1.0.0` and be incremented accordingly from then on.

## Error handling

The Connector SDK and all the specific PSP Connectors should hide the complexity of communicating to the PSP for Alfred. This includes, of course, having an excellent error handling for the requests.

This means that we should not let any exception bubble up to Alfred that is not previously processed by the connector. It may be that what the Connector does with the exception is to log and throw a new one with less or more generic information to Alfred. This new exception will be of the type `ConnectorException` or one of its children. 

Of course, whenever it makes sense, it's possible to throw exceptions from the connectors to Alfred. For example, when the validation of a request fails, a `RequestValidationException` should be thrown with the description of what exactly failed (with a list of every field failing and the reason for each one). 

An important note to make when we consider error handling in payment operations is that **rejected payment requests are not exceptions**. This is expected behavior and should be treated as a successful request with its result being indicated in a dedicated field inside the `OperationResponse` object. That being said, the `OperationException` should only be used for really unexpected behaviors that are not in the standard operation flow.

Another important note is that we should always keep in mind that debugging problems with so many external services becomes difficult. A way to reduce the risk here is to always log (or even better, store in some way) every request and every response. It's not uncommon that we are having a problem and the PSP denies it, so we need to have every available tool to discuss and fix it fast.

### Exception Classes

---

#### `ConnectorException`

Generic, module-level exception. It intends to wrap every possible exception thrown in the Connector SDK or the PSP Connectors, giving the user the ability to catch this exception and processing it in a standard way. For example, this exception (and its children) should always be easy to log with relevant information.

Despite this class not being abstract, it is preferred to use more specific children to indicate the category of the exception being thrown. This way, the developer can catch the specific one and handle them in different ways. For example, a request validation exception can be retried adding some missing fields, but an operation exception indicating that the PSP is down, should not be retried.

---

#### `RequestValidationException`

This is a specific exception thrown to indicate that the request is invalid according to the defined schema.

In case the validation fails for a given request, a `RequestValidationException` will be thrown. This class has two important fields. The `request` is the original request that the Connector received so that we can log it properly and compare different attempts. And the `validation` containing the details about the fields that failed and the reason why they failed.

For more information about request validation, check the [Request Validation](RequestValidation.md) section.
