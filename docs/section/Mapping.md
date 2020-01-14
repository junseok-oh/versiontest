# Mapping

In this section, we describe how to map:

- Alfred's payment request classes to the Connector SDK request classes
- Connector SDK response classes to Alfred's payment response classes
- Connector SDK request validation exception to Alfred's payment errors

However, what has to be decided for each specific PSP Connector, is the mapping of:

- Connector SDK to PSP request
- PSP response to Connector SDK response classes

For this mapping, there may be an SDK provided by the PSP or we may have to implement the direct HTTP call (or whatever is needed). In case there is an SDK available, the general recommendation is to use it. However, it's not uncommon that the PSP tech team is not maintaining the SDK or not updating it as fast as needed, so you should analyze case by case.

Regarding the mapping from Alfred's classes to the Connector SDK classes and vice-versa, we defined a hierarchy that is described in the following section.

> TODO: diagram with the Operation classes

## Operation Classes

For the operation mapping, the Connector SDK offers a group of classes that can be found under the `com.deliveryhero.alfred.connector.sdk.operation` package.

```
com.deliveryhero.alfred.connector.sdk.operation
├── Operation
├── OperationType
├── request
│   └── ...
└── response
    └── ...
```

Inside the `operation` package, we can find the `Operation` class and the `OperationType` enum that define the top-level view of every operation performed by the Connector.

We can also find the `request` and `response` subpackages here, which make a clear separation of the two main responsibilities of the Connectors.

---

### `Operation`

This abstract class represents the top-level perspective of an operation performed by the Connector. The aim is to be able to simplify the logging strategy by making this class very easy to log the complete information about what happened during the operation.

----

`type: OperationType`

The type of operation being performed.

----

`request: OperationRequest`

The original request made by Alfred for any of the available operations. Should always be an instance of its children (`AuthorizeRequest`, `RefundRequest`, etc.).

----

`response: OperationResponse`

The response from the PSP that was converted to an `OperationResponse` instance to be sent to Alfred.

---

### `OperationType`

This enum lists all the possible types of operations that can be performed in a PSP. Its main goal is to help developers follow the trace of a given operation in the logs.

It could be that some PSPs don't have some of them and also they may have some more. For every case, we should discuss if it makes sense to add them or to mark them as `UNKNOWN`.

The possible values can be divided into four groups. The first one is the payment operations that create or modify a payment, that is described in detail in the [Operations](Operations.md) section.

- `PREAUTHORIZE`
- `AUTHORIZE`
- `CANCEL`
- `CAPTURE`
- `REFUND`
- `CREDIT`

The second group is the transaction query operations:

- `GET`: Get the full information about a specific transaction in the PSP. Very useful to query its current status (captured, settled, chargebacks, etc.).
- `SEARCH`: Search the PSP transactions by a set of parameters like date range, amount, user, country, etc.
- `BALANCE`: Query the balance of a debit card or wallet account.

The last group is composed of:

- `TOKENIZE`: Tokenize a card or a user's account.
- `UNKNOWN`: Any other type of operation.

---

## Operation Response classes

```
com.deliveryhero.alfred.connector.sdk.operation.response
├── OperationResponse
├── OperationStatus
└── redirect
    ├── RedirectMethod
    ├── RedirectReason
    ├── RedirectRequest
    └── RedirectType
```

In the `operation.response` package, we can find the group of classes that map all the response data after a request was made to a PSP.

---

### `OperationResponse`

This class is a child of `Operation` that contains the translated response from the PSP to something that Alfred can understand. Also, it holds the original raw response code and the full body, for logging and debugging purposes.

----

`status: OperationStatus`

The status of the operation in the PSP after the request. The value of this field is obtained by translating the `rawStatus` field into one of the enum values. This mapping is defined in the [Mapping Table](https://docs.google.com/spreadsheets/d/1cc6mXTE5QBhe9JoHQPHuVcMw08CP9UElGZnr3ReT808/edit#gid=1324632352) and should be updated with every change.

----

`reference: String`

The unique identifier of the transaction in the PSP.

----

`redirectRequest: RedirectRequest`

If the PSP needs us to redirect the user to a URL, the needed information is in this object.

----

`rawStatus: String`

The original status field returned by the PSP. This field is useful for reporting and tracking.

----

`rawResponse: String`

The entire original raw response from the PSP. If possible, this should be a JSON string, so that it's easy to query it in the future.

---

### `OperationStatus`

- `OK`: The operation was successful.
- `CONFIG_ERROR`: There is a missing or invalid configuration for calling the PSP. This check is performed before trying to call.
- `OPERATION_NOT_ALLOWED`: The requested operation is not allowed by the Connector. The PSP is not called.
- `PARAMS_ERROR`: The PSP indicates that there is an error in the parameters we sent as a merchant. This is not related to the user or card used. For example, we sent incorrect merchant credentials.
- `UNKNOWN_ERROR`: There was an error, but we don't have it mapped. This should be treated as a priority to get more information and act accordingly (possibly updating the response mapping).
- `INVALID_CARD`: The card used is invalid for some reason not specified.
- `BLOCKED_CARD`: The card used is blocked by the issuer.
- `EXPIRED_CARD`: The card has expired or the expiration date is invalid.
- `FRAUD_RISK`: The transaction was rejected because of fraud risk.
- `INSUFFICIENT_FUNDS`: The card or account doesn't have enough balance for the transaction.
- `DO_NOT_HONOR`: The transaction is rejected by a generic reason.
- `PROVIDER_ERROR`: There was an internal error in the PSP or acquirer.
- `TIMEOUT`: We had a timeout in the communication. This should be quickly checked because maybe we charged the user and revert it quickly.
- `DUPLICATE_OPERATION`: The PSP rejected the operation because it's already done. Maybe there is a unique check in some of the request fields.
- `REDIRECT_REQUIRED`: The PSP requests that we redirect the user to a specific URL to continue the payment process. In this case, there should be a value in the `redirectRequest` field of the `OperationResponse` object returned.

---

### `RedirectRequest`

This class is used when a PSP requires us to redirect the user to a specific URL for completing the transaction. It could be triggered by a 3DS flow, an end-user confirmation, a tokenization request, etc.

----

`redirectUrl: String`

The URL where we need to redirect the user to continue the payment process.

----

`redirectMethod: RedirectMethod`

The HTTP method we need to use for the redirect.

----

`redirectType: RedirectType`

The type of redirection we need to perform.

----

`redirectReason: RedirectReason`

The reason for the redirection.

---

### `RedirectMethod`

The HTTP method we need to use when redirecting the user to the specified URL.

- `GET`
- `POST`

---

### `RedirectType`

The type of redirection we need to perform in the client-facing application.

- `IFRAME`: Open the `redirectUrl` in an iFrame inside our site.
- `FULL`: Fully redirect the user to the specified URL in the current tab.
- `DEEPLINK`: Only used for mobile apps. Open the URL as a deeplink in a previously installed application.

---

### `RedirectReason`

The reason why the PSP is requesting a redirection.

- `CONFIRMATION`: The user needs to confirm the transaction before continuing.
- `THREE_DS`: The redirection is part of a 3DS flow triggered.
- `TOKENIZATION`: The request for tokenizing a card or account needs the user confirmation.
- `OTHER`: Unknown or unmapped reason for redirection. Should be checked to analyze strange PSP behaviors.

---

## Operation Request classes

```
com.deliveryhero.alfred.connector.sdk.operation.request
├── AuthorizeRequest
├── CaptureRequest
├── CreditRequest
├── GetRequest
├── ModificationRequest
├── OperationRequest
├── RefundRequest
├── TokenizationRequest
├── CancelRequest
├── common
│   └── ...
├── config
│   └── ...
├── paymentinstrument
│   └── ...
├── redirect
│   └── ...
└── validation
    └── ...
```

In the `operation.request` package, we can find the group of classes that map all the possible types of requests to a PSP. The `OperationRequest` class, a child of `Operation`, serves as the top-level class in this package, with one child per type of operation defined. The `common`, `config`, `paymentinstrument` and `redirect` packages contain other classes that help build the request objects.

---

### `OperationRequest`

This abstract class is a child of `Operation` and will be a top-level class for all operation requests. Also, it holds the original raw request that we sent to the PSP, for logging and debugging purposes.

----

`type: OperationType`

The type of operation to be executed.

----

`providerConfig: ProviderConfig`

The configuration needed for calling the PSP.

----

`rawRequest: String`

This field should hold a JSON representation of the rest of the properties in this class. The aim is to be able to log a detailed version of what we sent to the PSP.

---

### `AuthorizeRequest`

This class is a child of `OperationRequest` and holds the information needed for a preauthorization or an authorization (the type should be specified).

----

`customer: Customer`

Information about the customer performing the payment.

----

`order: Order`

Information about the order that needs a payment.

----

`transaction: Transaction`

Information about the payment transaction.

----

`paymentInstrument: PaymentInstrument`

Information about the instrument used for the payment (credit or debit card, external account, etc.)

----

`vendor: Vendor`

Information about the vendor of the order.

----

`shouldStorePaymentInstrument: Boolean`

Flag indicating if we should store the payment instrument for the consecutive future operations of this customer.

---

### `ModificationRequest`

This abstract class is a child of `OperationRequest` and serves as top-level to all the payment modification operations.

----

`originalOperationId: String`

The reference to the original transaction we need to modify. This value should be equal to the `reference` value returned for the original request.

----

`operationId: String`

The reference to the modification operation that we are performing.

---

### `CaptureRequest`

This is a child of `ModificationRequest`. We use it when we want to capture a previously preauthorized transaction.

For more details about the payment flow, check the [Operations](Operations.md) section.

----

`amount: Money`

The amount we want to capture. If not specified, the original amount preauthorized is assumed.

---

### `CancelRequest`

This is a child of `ModificationRequest`. We use it when we want to cancel (or void) a previously preauthorized transaction.

For more details about the payment flow, check the [Operations](Operations.md) section.

---

### `RefundRequest`

This is a child of `ModificationRequest`. We use it when we want to refund a previously captured or authorized transaction.

For more details about the payment flow, check the [Operations](Operations.md) section.

----

`amount: Money`

The amount we want to refund. If not specified, a total refund is assumed.

---

### `CreditRequest`

This is a child of `OperationRequest`. We use it when we want to transfer money into a card or account.

----

`paymentInstrument: PaymentInstrument`

The payment instrument we want to credit.

----

`amount: Money`

The amount we want to credit.

---

### `TokenizationRequest`

This is a child of `OperationRequest`. We use it when we want to tokenize (store sensible data in an external provider) for future use. For example, when we want to associate an external wallet account to our system.

----

`approvalToken: String`

A token returned by the PSP that identifies the account being tokenized.

---

### `GetRequest`

This is a child of `OperationRequest`. We use it for finding a transaction in the PSP by its reference. This operation is very useful for checking the current status of a transaction, especially when we got some kind of timeout that left the transaction in an unknown status on our side.

----

`reference: String`

The reference to the transaction we are trying to find.

---

## Request building classes

This section describes the classes that help build requests for the PSPs clearly and understandably. The fields in these classes are all optional because the constraints for them are defined in the validation schema specific to each PSP and request.

---

### Common package

The classes in this package are the most commonly required by PSPs for payment operations.

```
com.deliveryhero.alfred.connector.sdk.operation.request.common
├── Address
├── Country
├── Currency
├── Customer
├── Money
├── Order
├── Phone
├── ShippingType
├── Transaction
└── Vendor
```

---

### `Address`

Information about an address. It could be used as the delivery or billing address of the customer or the address of the vendor.

----

`street: String`

Name of the street. It can include the street type and door number if separating them is not needed.

----

`streetType: String`

Type of street (Avenue, Boulevard, etc.). It could be skipped if already specified as part of the `street`.

----

`complement: String`

Optional complement for the address. Sometimes it's referred to as `addressLine2` or `street2`.

----

`doorNumber: String`

Door number of the address. It could be skipped if already specified as part of the `street`.

----

`area: String`

Name of the area inside the city. It could be a neighborhood.

----

`city: String`

Name of the city of the address. It should be bigger than the `area` and smaller than the `state`.

----

`state: String`

Name of the state of the address. It should be bigger than the `city` and smaller than the `country`.

----

`country: Country`

Country of the address.

----

`postalCode: String`

Postal code of the address. Also known as `zipCode` for some PSPs.

----

`latitude: Double`

Latitude of the address. Should always go together with a `longitude`. Especially useful for fraud prevention.

----

`longitude: Double`

Longitude of the address. Should always go together with a `latitude`. Especially useful for fraud prevention.

---

### `Country`

Enum containing a list of countries. The list is defined by the [ISO 3166-1](https://www.iso.org/iso-3166-country-codes.html) in the Alpha-2 form. If any country is missing, it can be added per request.

----

`iso3: String`

The Alpha-3 form of the country. For example, for Uruguay, the Alpha-2 is `UY` and the Alpha-3 is `URY`.

----

`fullName: String`

The country's full name in English.

---

### `Currency`

Enum containing a list of currencies. The list is defined by the [ISO 4217](https://www.iso.org/iso-4217-currency-codes.html). If any currency is missing, it can be added per request.

----

`fullName: String`

The currency's full name in English.

----

`precision: Int`

The precision of the currency. The default value is `2` because most currencies have two decimals precision. However, the Japanese Yen, for example, doesn't use decimals, so its precision is `0`.

This value should be overridden if the PSP specifies a different precision for a currency.

---

### `Customer`

This class collects information about the customer performing the payment.

----

`id: String`

The identifier for the customer. This will usually be the identifier in the entity calling Alfred. However, it can also be Alfred's user id, the wallet id, or even an email.

----

`name: String`

First name of the customer. It can also be the full name if the PSP doesn't separate the last name into another field.

----

`lastName: String`

Last name of the customer. It can be empty if the last name was already included in the `name` field.

----

`email: String`

Email of the customer. This is usually unique-constrained in the PSPs.

----

`phone: Phone`

The phone information of the customer.

----

`birthDate: String`

The birth date of the customer in the `yyyy-MM-dd` format. This is usually optional in the PSPs but can be used as a verification for fraud.

----

`language: String`

The language of the customer. Usually in the two characters format, but depends on the PSP requirements.

----

`country: Country`

Country of origin of the customer. It can be different than the delivery or billing addresses.

----

`fullName: String`

This is a magic method for getting the full name when you have the `name` and `lastName` separated.

----

`locale: String`

The locale of the customer. Useful when a redirect is needed, if the PSP allows changing the locale.

---

### `Money`

This class describes an amount with a specific currency.

----

`currency: Currency`

The currency of the amount.

----

`value: BigDecimal`

The actual amount value. Should be a `BigDecimal` (or similar) to allow decimals.

---

### `Order`

This class collects information about the order for which we need a payment.

----

`id: String`

Identifier of the order in the platform. Usually called `orderId` or `orderCode`. It can be repeated (if allowed by the PSP) because of multiple payment attempts of the same order.

----

`description: String`

Short text description of the order.

----

`brandName: String`

Name of the brand performing the order. For example: PedidosYa, Domicilios, Foodpanda, Foodora, etc.

----

`deliveryAddress: Address`

Address where the order should be delivered. It can be `null` for pickup orders.

----

`shippingType: ShippingType`

Type of shipping for the order.

---

### `Phone`

This class describes a phone number with its type.

----

`type: PhoneType`

Type of the phone.

----

`number: String`

Phone number. It should usually be in the international format (including the country code prefix), but this depends on each PSP requirements.

---

### `PhoneType`

This enum describes the types of phone numbers we support.

- `MOBILE`: Mobile phone.
- `HOME`: Home phone. Also known as landline or fixed phone.

---

### `ShippingType`

This enum describes the options for the shipping of an order.

- `DELIVERY`: The order will be delivered to the specified delivery address.
- `PICKUP`: The customer will pick up the order from the vendor's location.

---

### `Transaction`

This class contains information about the payment transaction we want to perform.

----

`id: String`

Public identifier of the payment transaction in Alfred.

----

`type: OperationType`

Type of the operation to be performed in the PSP.

----

`amount: Money`

Amount of the transaction with currency.

----

`billingAddress: Address`

Billing address of the customer for this transaction.

----

`softDescriptor: String`

The text that will appear in the customer's bank statement.

----

`returnUrlInfo: ReturnUrlInfo`

If the PSP determines that a redirection is needed to complete the payment, the return URL information should be in this object.

----

`redirectResponse: RedirectResponse`

If this is a call informing about the result of a redirect process, the information sent from the PSP should be in this object.

---

### `Vendor`

This class contains information about the vendor preparing the order. Usually useful for fraud prevention purposes.

----

`id: String`

Identifier of the vendor on the platform.

----

`name: String`

Name of the vendor.

----

`address: Address`

Address information of the vendor.

---

### Config package

The classes in this package are concerned with the configuration of the PSP. For example, the environment where we want to execute a transaction, the credentials of our merchant account, etc.

```
com.deliveryhero.alfred.connector.sdk.operation.request.config
├── Environment
└── ProviderConfig
```

---

### `ProviderConfig`

This class contains general configuration information in the PSP.

----

`identifier: String`

Identifier of the PSP in Alfred.

----

`environment: Environment`

The environment in which this request should be executed.

----

`config: Map<String, String>`

Map of configurations to be used by the Connector.

---

### `Environment`

This enum defines the environments where requests are executed.

- `LOCAL`: A developer's personal computer.
- `TEST`: Test environment in the PSP.
- `LIVE`: Live environment in the PSP.

---

### PaymentInstrument package

The classes in this package are concerned with the representation of the different types of payment instruments that can be used for performing a payment.

```
com.deliveryhero.alfred.connector.sdk.operation.request.paymentinstrument
├── Card
├── EncryptedCard
├── EncryptedCardByField
├── ExternalAccount
├── PaymentInstrument
├── PaymentInstrumentType
├── RawCard
└── TokenizedCard
```

---

### `PaymentInstrumentType`

This enum describes the types of payment instruments that can be used for payment.

- `RAW_CARD`: Card with sensible information in plain-text.
- `TOKENIZED_CARD`: Card represented by a token.
- `ENCRYPTED_CARD`: Card encrypted in the client-side.
- `ENCRYPTED_CARD_BY_FIELD`: Card encrypted in the client-side field by field.
- `EXTERNAL_ACCOUNT`: External account (PayPal, other wallets, etc.).

---

### `PaymentInstrument`

Abstract base class for payment instruments.

----

`type: PaymentInstrumentType`

Type of the payment instrument.

----


`displayValue: String`

Public information which can be used to display the payment instrument on the client-side.

---

### `Card`

This abstract class is a child of `PaymentInstrument` and contains common information for all cards.

----

`bin: String`

The BIN (Bank Identification Number) of the card. It can be also known as IIN (Issuer Identification Number) or simply the first 6 or 8 digits of the card number.

----

`lastDigits: String`

The last 4 digits of the card. This is usually the public information shown to the user on the client-side.

----

`expiryMonth: String`

Expiration month of the card. The format is usually `mm` but can change for each PSP.

----

`expiryYear: String`

Expiration year of the card. The format is usually `yyyy` but can change for each PSP.

----

`holderName: String`

Name of the owner of the card as it appears in the front or back of the card.

---

### `EncryptedCard`

This class is a child of `Card` and represents card information that was encrypted in the client-side to be passed to the PSP without accessing the sensible information.

----

`encryptedData: String`

The encrypted data of the card in String representation.

---

### `EncryptedCardByField`

This class is a child of `Card` and represents card information that was encrypted in the client-side to be passed to the PSP without accessing the sensible information. Similar to the `EncryptedCard` but in this case the sensible data was encrypted field by field.

----

`encryptedNumber: String`

The encrypted card number.

----

`encryptedExpiryMonth: String`

The encrypted card expiration month.

----

`encryptedExpiryYear: String`

The encrypted card expiration year.

----

`encryptedSecurityCode: String`

The encrypted card security code. It is also known as CVV, CVC, etc.

----

`encryptedHolderName: String`

The encrypted cardholder name.

---

### `TokenizedCard`

This class is a child of `Card` and represents a card that was stored in some external vault (maybe the PSP itself) and we have a token representing it.

----

`token: String`

The token identifying the card in the vault.

----

`securityCode: String`

In some cases, the user needs to re-enter the security code for a tokenized payment.

---

### `RawCard`

This class is a child of `Card` and represents a card with sensible data in plain-text. This should only be used in testing environments.

----

`number: String`

Full card number in plain-text. Also known as PAN (Permanent Account Number).

----

`securityCode: String`

The 3 or 4 digit code behind or in front of the card. Also known as CVV (Card Verification Value), CVC (Card Verification Code), CVC2 (Mastercard), CID (Amex), CID2 (Diners), CVV2 (Visa), and who knows when they will come up with a new one.

---

### `ExternalAccount`

We want to be able to integrate Alfred with as many payment methods as possible. This class is a child of `PaymentInstrument` that represents an account of the customer in some external system. It could be PayPal, AliPay, or any other.

----

`externalUserId: String`

Identifier of the user in the external system.

----

`externalAccountId: String`

Identifier of the user's account in the external system.

---

### Redirect package

The classes in this package are used for indicating the PSP a `returnUrl` if needed for user confirmation, 3DS flow or tokenization. This value should always be provided if not configured in the PSP.

```
com.deliveryhero.alfred.connector.sdk.operation.request.redirect
├── RedirectResponse
└── ReturnUrlInfo
```

---

### `ReturnUrlInfo`

This class contains the information provided by the platform in case the PSP needs us to redirect the customer to a redirect URL.

----

`success: String`

The URL where the PSP should send the customer if the transaction was successful. This URL can also be used as the single return URL in case the PSP informs about the result in some other way (a query parameter or body).

----

`error: String`

The URL where the PSP should send the customer if there was an error in the transaction.

----

`cancel: String`

The URL where the PSP should send the customer if they canceled the process.

---

### `RedirectResponse`

When the PSP finished the redirect process with the customer, they inform us about the result by calling a URL defined in a `ReturnUrlInfo` object. This class is used to hold the information sent by the PSP in that call.

----

`success: Boolean`

If possible, the client can be informed if the transaction was successful or not.

----

`params: Map<String, String>`

The full list of parameters received in the `returnUrl` call. It can be headers, query or body parameters.

---

### Validation package

The classes in this package handle the validation of the request about to be sent to the PSP.

For more information about request validation, check the [Request Validation](RequestValidation.md) section.
