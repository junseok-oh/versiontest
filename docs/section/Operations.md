# Operations

Given that the Connectors aim to allow Alfred to connect with different PSPs reducing the impact on its code, we need to define which operations can be performed on those PSPs.

The `ConnectorPaymentGateway` interface is maybe the most important piece of the Connector SDK. This interface is the contract that every PSP Connector must comply with to interact with Alfred. In other words, this is the entry point for every operation on a Connector.

The interface defines two distinct groups of operations. First, the operations that describe the PSP for Alfred and determine if the PSP is usable for a certain context. Second, the payment operations that the PSP can execute regarding payments.

## PSP Description Functions

This group of operations helps Alfred identify and describe the PSP that is behind the Connector.

----

`getIdentifierName(): String`

This function returns the name of the PSP that the Connector interacts with. For example: "PayPal".

----

`getIdentifierVersion(): String`

This function returns the version of the integration to the PSP. Having different internal versions of integrations, allows us to manage multiple connections to the same PSP supporting older or different services. It's important to note that this version is not necessarily the same as the PSP service version.

----

`getPaymentMethod(): String`

This function returns the name of the payment method defined in Alfred. This value should rarely change (or change with coordination with the Alfred team).

----

`isApplicableFor(config: ProviderConfig): Boolean`

This function returns true if this PSP can be used for a payment operation given the configuration passed as a parameter and any other condition defined internally. 

For example, payment methods in Alfred can be enabled or disabled by configuration, they can be applied for specific countries or currencies, they can have a minimum or maximum transaction amount, etc.

---

## Payment Functions

Before using the next group of functions, you need to have a clear understanding of the payment operations. They are defined in detail in the [Payment Operations](#Payment Operations) section.

----

`preauthorize(request: AuthorizeRequest): OperationResponse`

This function performs a **preauthorization** using an `AuthorizeRequest` object, returning an `OperationResponse` object or throwing a `ConnectorException`.

----

`authorize(request: AuthorizeRequest): OperationResponse`

This function performs an **authorization** using an `AuthorizeRequest` object, returning an `OperationResponse` object or throwing a `ConnectorException`.

----

`cancel(request: CancelRequest): OperationResponse`

This function performs a **cancel** operation using an `CancelRequest` object, returning an `OperationResponse` object or throwing a `ConnectorException`.

----

`capture(request: CaptureRequest): OperationResponse`

This function performs a **capture** operation using an `CaptureRequest` object, returning an `OperationResponse` object or throwing a `ConnectorException`.

----

`refund(request: RefundRequest): OperationResponse`

This function performs a **refund** operation using an `RefundRequest` object, returning an `OperationResponse` object or throwing a `ConnectorException`.

----

`credit(request: CreditRequest): OperationResponse`

This function performs a **credit** operation using an `CreditRequest` object, returning an `OperationResponse` object or throwing a `ConnectorException`.

---

# Payment Operations

When a merchant wants to charge a customer for a product or service, they need an operation that allows them to move funds from the customer's account (a card, bank, wallet, etc.) to their account (usually an acquirer bank or other financial institution).

The entity that provides the service that allows a merchant to request payments is called a Payment Service Provider, usually abbreviated as PSP. Depending on the maturity of this service and the context where it works (country, card brands, etc.), they offer more or fewer features.

The main operation for payments is called **authorization** (also known as sale, buy, or simply payment). This means that the merchant sends information (customer, order, address, instrument, vendor, etc.) to a PSP requesting a certain amount of money to be moved to their account in exchange for a product or service offered by the merchant to the customer. The PSP then performs all the necessary checks and also communicates with other entities to obtain a final response about the result of the operation. For example, they need to check that the card is valid, not blocked, has funds, is not flagged as fraudulent, etc.

This sounds simple, right? But what happens if for some reason the merchant has to cancel the order? In this case, the merchant has to request a **refund** process on the payment transaction. This refund can be full, all the original money is moved back to the customer's account; or partial, where the merchant specifies a specific amount to refund. The partial refund is especially useful when the customer complains about not receiving all the items they originally requested.

If this still sounds simple, let's complicate it a bit more. Refunds can take a lot of time to be reflected in the customer's account, and this leads to bad user experience and possibly the customer will not buy again from that merchant. For this reason, the **preauthorization** (also known as reservation) operation exists. This operation is almost the same as the authorization; the same information is sent in the request by the merchant and the same checks are performed by the PSP and the other entities. However, there is a small difference. Instead of moving the money immediately from one account to the other, the money is reserved for a certain amount of time (or until the merchant captures or cancels it). This is typically what hotels or car rental companies do.

In this case, if the merchant or the customer cancels the order, we just have to **cancel** (also known as reverse or void) the preauthorization and the reserved money is free to use for other purchases again in the customer's account. In other words, the money never left the customer's account, so it's simply forgetting about the reservation made for the order.

However, if the merchant executes a **capture** on the preauthorization, the money will move to the merchant's account, as in the normal authorization operation. In some scenarios, the merchant is allowed to capture a different amount than the preauthorization, with the risk of being rejected. After the money is captured, a refund can be executed in the same way as explained before.

We usually call the scenarios that allow only authorization and refund a **one-step payment**. For the scenarios where a preauthorization, cancel, capture, and refund are allowed, we use the term **two-step payment**.

Finally, in some cases, we are also allowed to perform a **credit** operation. In this case, the merchant puts money into the customer's account. We can think about it as a payment to the customer. Credit cards usually don't allow this operation, but it's more common for wallets like PayPal.
