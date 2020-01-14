package com.deliveryhero.alfred.connector

import com.adyen.constants.ApiConstants
import com.adyen.model.checkout.CheckoutPaymentsAction
import com.adyen.model.checkout.PaymentsResponse
import com.adyen.model.checkout.Redirect
import com.adyen.model.modification.ModificationResult
import com.deliveryhero.alfred.connector.adyen.common.AdyenGatewayTest
import com.deliveryhero.alfred.connector.adyen.common.dom.AdyenConfigData
import com.deliveryhero.alfred.connector.adyen.common.util.AdyenResponseParser.ADDITIONAL_DATA_RECURRING_DETAIL_REFERENCE
import com.deliveryhero.alfred.connector.sdk.exception.ConnectorException
import com.deliveryhero.alfred.connector.sdk.operation.OperationType
import com.deliveryhero.alfred.connector.sdk.operation.request.*
import com.deliveryhero.alfred.connector.sdk.operation.request.common.*
import com.deliveryhero.alfred.connector.sdk.operation.request.config.Environment
import com.deliveryhero.alfred.connector.sdk.operation.request.config.ProviderConfig
import com.deliveryhero.alfred.connector.sdk.operation.request.paymentinstrument.ExternalAccount
import com.deliveryhero.alfred.connector.sdk.operation.request.paymentinstrument.OneTimePayment
import com.deliveryhero.alfred.connector.sdk.operation.request.paymentinstrument.PaymentInstrument
import com.deliveryhero.alfred.connector.sdk.operation.request.paymentinstrument.TokenizedCard
import com.deliveryhero.alfred.connector.sdk.operation.request.redirect.RedirectResponse
import com.deliveryhero.alfred.connector.sdk.operation.request.redirect.ReturnUrlInfo
import com.deliveryhero.alfred.connector.sdk.operation.response.redirect.RedirectReason
import com.deliveryhero.alfred.connector.sdk.operation.response.redirect.RedirectRequest
import org.junit.jupiter.api.Assertions
import java.math.BigDecimal
import kotlin.random.Random

fun mockProviderConfig(identifier: String, shouldSendBrowserInfo: Boolean? = null): ProviderConfig {
    return AdyenConfigData(
        when (identifier) {
            "adyen_applepay" -> AdyenGatewayTest.PAYMENT_METHOD_APPLEPAY
            "adyen_googlepay" -> AdyenGatewayTest.PAYMENT_METHOD_GOOGLEPAY
            "adyen" -> AdyenGatewayTest.DEFAULT_PROVIDER_IDENTIFIER
            else -> throw ConnectorException("Identifier not recognized by AdyenConfig")
        },
        environment = Environment.TEST,
        config = mutableMapOf(
            AdyenConfigData.API_KEY to (System.getenv("API_KEY") ?: Assertions.fail("API_KEY is missing")),
            AdyenConfigData.MERCHANT_ACCOUNT to (System.getenv("MERCHANT_ACCOUNT")
                ?: Assertions.fail("MERCHANT_ACCOUNT is missing")),
            AdyenConfigData.SHOULD_SEND_BROWSER_INFO to (shouldSendBrowserInfo.toString())

        )
    )
}

fun mockAuthorizeRequest(
    transaction: Transaction = mockTransaction(OperationType.AUTHORIZE),
    paymentInstrument: PaymentInstrument? = mockTokenizedCard(),
    order: Order = mockOrder(),
    shouldStorePaymentInstrument: Boolean = false,
    providerConfig: ProviderConfig = mockProviderConfig(AdyenGatewayTest.DEFAULT_PROVIDER_IDENTIFIER),
    clientContext: ClientContext = mockClientContext(),
    additionalData: Map<String, String>? = null
): AuthorizeRequest {
    return AuthorizeRequest(
        customer = mockCustomer(),
        order = order,
        transaction = transaction,
        paymentInstrument = paymentInstrument,
        vendor = mockVendor(),
        providerConfig = providerConfig,
        shouldStorePaymentInstrument = shouldStorePaymentInstrument,
        clientContext = clientContext,
        additionalData = additionalData
    )
}

fun mockClientContext(
    ipAddress: String? = "1.1.1.1",
    userAgent: String? = "Mozilla/5.0 (Macintosh; Intel Mac OS X x.y; rv:42.0) Gecko/20100101 Firefox/42.0",
    acceptHeader: String? = "application/json"
): ClientContext {
    return ClientContext(ipAddress, userAgent, acceptHeader)
}

fun mockCaptureRequest(
    originalOperationId: String = "defOrigRef123",
    amount: Money?,
    providerConfig: ProviderConfig = mockProviderConfig(AdyenGatewayTest.DEFAULT_PROVIDER_IDENTIFIER)
): CaptureRequest {
    return CaptureRequest(
        operationId = "",
        originalOperationId = originalOperationId,
        providerConfig = providerConfig,
        amount = amount
    )
}

fun mockCancelRequest(
    originalOperationId: String = "defOrigRef123",
    providerConfig: ProviderConfig = mockProviderConfig(AdyenGatewayTest.DEFAULT_PROVIDER_IDENTIFIER)
): CancelRequest {
    return CancelRequest(
        operationId = "",
        originalOperationId = originalOperationId,
        providerConfig = providerConfig
    )
}

fun mockCreditRequestForCard(
    amount: Long = (1 + Random.nextLong(10)).apply { println("Amount: $this") },
    providerConfig: ProviderConfig = mockProviderConfig(AdyenGatewayTest.DEFAULT_PROVIDER_IDENTIFIER)
): CreditRequest {
    return CreditRequest(
        paymentInstrument = mockTokenizedCard(),
        amount = mockMoney(amount),
        providerConfig = providerConfig
    )
}

fun mockCreditRequestForOneTimePayment(
    amount: Long = (1 + Random.nextLong(10)).apply { println("Amount: $this") },
    providerConfig: ProviderConfig = mockProviderConfig(AdyenGatewayTest.DEFAULT_PROVIDER_IDENTIFIER)
): CreditRequest {
    return CreditRequest(
        paymentInstrument = mockOneTimePayment(),
        amount = mockMoney(amount),
        providerConfig = providerConfig
    )
}

fun mockRefundRequest(
    originalOperationId: String,
    amount: Money,
    providerConfig: ProviderConfig = mockProviderConfig(AdyenGatewayTest.DEFAULT_PROVIDER_IDENTIFIER)
): RefundRequest {
    return RefundRequest(
        operationId = "",
        originalOperationId = originalOperationId,
        providerConfig = providerConfig,
        amount = amount
    )
}

fun mockCustomer(): Customer {
    return Customer(
        id = "12345",
        name = "John",
        lastName = "Doe",
        email = "john.doe@example.com",
        phone = mockPhone(),
        birthDate = "1980-01-01"
    )
}

fun mockPhone(): Phone {
    return Phone(
        type = PhoneType.MOBILE,
        number = "4917586050557"
    )
}

fun mockAddress(
    street: String? = "SentszStraße",
    complement: String? = "under the bridge",
    doorNumber: String? = "doorNumber",
    area: String? = "Berlin",
    city: String? = "Berlin",
    state: String? = "Berlin",
    country: Country? = Country.DE,
    postalCode: String? = "10100"
): Address {
    return Address(
        street = street,
        complement = complement,
        doorNumber = doorNumber,
        area = area,
        city = city,
        state = state,
        country = country,
        postalCode = postalCode
    )
}

fun mockOrder(deliveryAddress: Address = mockAddress()): Order {
    return Order(
        id = Random.nextLong().toString(), // "54321",
        description = "Order 1",
        brandName = "Brand 1",
        deliveryAddress = deliveryAddress,
        shippingType = ShippingType.DELIVERY
    )
}

fun mockTransaction(
    operationType: OperationType,
    returnUrlInfo: ReturnUrlInfo? = null,
    redirectResponse: RedirectResponse? = null,
    currency: Currency = Currency.EUR,
    amount: Long = (1 + Random.nextLong(10)).apply { println("Amount: $this") }
): Transaction {
    return Transaction(
        id = "98765",
        type = operationType,
        amount = mockMoney(amount, currency),
        softDescriptor = "FoodPanda",
        returnUrlInfo = ReturnUrlInfo("https://example.com/success"),
        billingAddress = mockAddress(),
        redirectResponse = redirectResponse
    )
}

fun mockMoney(amount: Long, currency: Currency = Currency.EUR): Money {
    return Money(
        currency = currency,
        value = BigDecimal.valueOf(amount)
    )
}

fun mockRedirectRequest(): RedirectRequest {
    return RedirectRequest(
        redirectUrl = "https://example.com/success",
        redirectReason = RedirectReason.TOKENIZATION
    )
}

fun mockReturnUrlInfo(): ReturnUrlInfo {
    return ReturnUrlInfo(
        success = "https://example.com/success",
        error = "https://example.com/error",
        cancel = "https://example.com/cancel"
    )
}

fun mockRedirectResponse(): RedirectResponse {
    return RedirectResponse(
        success = true,
        params = mutableMapOf() //mutableMapOf("ORDER_ID" to "54321")
    )
}

fun mockPaymentsResponse(
    resultCode: PaymentsResponse.ResultCodeEnum,
    refusalReason: String? = null
) = PaymentsResponse().apply {
    resultCode(resultCode)
    refusalReason(refusalReason)
    pspReference = "ref123"
    action = CheckoutPaymentsAction()
    action.data = mapOf("MD" to "md", "PaReq" to "paReq")
    redirect = Redirect()
    redirect?.url = "issuerUrl"
    additionalData = mapOf(
        ADDITIONAL_DATA_RECURRING_DETAIL_REFERENCE to "token123",
        ApiConstants.AdditionalData.EXPIRY_DATE to "10/2030",
        ApiConstants.AdditionalData.CARD_HOLDER_NAME to "Card Holder",
        ApiConstants.AdditionalData.PAYMENT_METHOD to "visa",
        ApiConstants.AdditionalData.CARD_SUMMARY to "1111",
        ApiConstants.AdditionalData.CARD_BIN to "411111"
    )
}

fun mockModificationResult(): ModificationResult {
    val result = ModificationResult()
    result.response = ModificationResult.ResponseEnum.CANCELORREFUND_RECEIVED_
    result.pspReference = "ref123"
    return result
}

fun mockTokenizedCard(): TokenizedCard {
    return TokenizedCard(
        bin = "12345",
        lastDigits = "0987",
        expiryMonth = "11",
        expiryYear = "2025",
        holderName = "John Doe",
        displayValue = "My Card",
        token = ""
    )
}

fun mockTockenizedCardWithIncorrectExpiryMonth(): TokenizedCard {
    return TokenizedCard(
        bin = "12345",
        lastDigits = "0987",
        expiryMonth = "20",
        expiryYear = "2025",
        holderName = "John Doe",
        displayValue = "My Card",
        token = ""
    )
}

fun mockExternalAccount(userId: String? = null, token: String): ExternalAccount {
    return ExternalAccount(
        externalUserId = userId,
        paymentInstrumentToken = token,
        displayValue = "My Account"
    )
}

fun mockOneTimePayment(token: String = "default_value"): OneTimePayment {
    return OneTimePayment(token)
}

fun mockVendor(): Vendor? {
    return Vendor(
        id = "4444",
        name = "Vendor 1",
        address = mockAddress()
    )
}
