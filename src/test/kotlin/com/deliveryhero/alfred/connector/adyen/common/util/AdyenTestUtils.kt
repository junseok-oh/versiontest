package com.deliveryhero.alfred.connector.adyen.common.util

import com.deliveryhero.alfred.connector.sdk.operation.request.AuthorizeRequest
import com.deliveryhero.alfred.connector.sdk.operation.request.common.PhoneType

/**
 * Test utils for Adyen integration.
 *
 * @author mathifonseca
 */
object AdyenTestUtils {
    //TODO:: Delete this class when Adyen credit card moves out of here. Left here for reference only. Always use MockitoExtension for new connectors
    val TEST_MERCHANT_ACCOUNT = "default_account"
    val TEST_API_KEY = "default_key"
    val DEFAULT_AMOUNT = 100L
    val DEFAULT_MERCHANT_ACCOUNT = "m123"
    val DEFAULT_API_KEY = "api123"
    val DEFAULT_REFERENCE = "ref123"
    //   val DEFAULT_CURRENCY = "UYU"
    val DEFAULT_CARD_SCHEME = "visa"
    val DEFAULT_CARD_HOLDER = "Card Holder"
    val DEFAULT_CARD_SUMMARY = "1111"
    val DEFAULT_CARD_BIN = "411111"
    val DEFAULT_CARD_EXPIRY_DATE = "10/2030"
    val DEFAULT_CARD_EXPIRY_MONTH = "10"
    val DEFAULT_CARD_EXPIRY_YEAR = "2030"

    val DEFAULT_USER_ID = "user123"
    val DEFAULT_USER_EMAIL = "test@deliveryhero.com"
    val DEFAULT_USER_PHONE_TYPE = PhoneType.MOBILE
    val DEFAULT_USER_PHONE = "12341234"
    val DEFAULT_FIRST_NAME = "Test"
    val DEFAULT_LAST_NAME = "User"
    val DEFAULT_USER_IP_ADDRESS = "1.1.1.1"
    val DEFAULT_USER_USERAGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X x.y; rv:42.0) Gecko/20100101 Firefox/42.0"
    val DEFAULT_USER_ACCEPT_HEADER = "application/json"
    val DEFAULT_USER_PAYMENT_INTRUMENT_TOKEN = "PT-ABCD1234"

    val DEFAULT_ORIGINAL_REFERENCE = "oRef123"
    val DEFAULT_TOKEN = "token123"
    val DEFAULT_MD = "md"
    val DEFAULT_PAREQ = "paReq"
    val DEFAULT_PARES = "paRes"
    val DEFAULT_GLOBAL_PAYMENT_TRANSACTION_ID = "globalPaymentTransaction123"
    val DEFAULT_PLATFORM_REFERENCE = "platformReference123"
    val TEST_CALLBACK_HMAC_KEY = "00E0A65CB3DB618ED905C9F16EA1D1205C0368458EFB0D1CDBC3B1431822C167"
    val FAILURE_CALLBACK_RAW_REQUEST =
        "{\"live\":\"false\",\"notificationItems\":[{\"NotificationRequestItem\":{\"additionalData\":{\"cardSummary\":\"7777\",\"eci\":\"N\\/A\",\"shopperIP\":\"127.0.0.1\",\"totalFraudScore\":\"10\",\"hmacSignature\":\"9Vhx8Xt706Z793+7fiugH2\\/YOaYxCmRdDhUvKLXkIQo=\",\"expiryDate\":\"12\\/2012\",\"xid\":\"AAE=\",\"billingAddress.street\":\"Nieuwezijds Voorburgwal\",\"cavvAlgorithm\":\"N\\/A\",\"cardBin\":\"976543\",\"extraCostsValue\":\"102\",\"billingAddress.city\":\"Amsterdam\",\"threeDAuthenticated\":\"false\",\"paymentMethodVariant\":\"visa\",\"billingAddress.country\":\"NL\",\"fraudCheck-6-ShopperIpUsage\":\"10\",\"deviceType\":\"Other\",\" NAME1 \":\"VALUE1\",\"cardHolderName\":\"J. De Tester\",\"threeDOffered\":\"false\",\"billingAddress.houseNumberOrName\":\"21 - 5\",\"threeDOfferedResponse\":\"N\\/A\",\"NAME2\":\"  VALUE2  \",\"billingAddress.postalCode\":\"1012RC\",\"browserCode\":\"Other\",\"cavv\":\"AAE=\",\"issuerCountry\":\"unknown\",\"threeDAuthenticatedResponse\":\"N\\/A\",\"extraCostsCurrency\":\"EUR\"},\"amount\":{\"currency\":\"EUR\",\"value\":10150},\"eventCode\":\"AUTHORISATION\",\"eventDate\":\"2019-10-09T17:46:08+02:00\",\"merchantAccountCode\":\"Foodpanda_BD\",\"merchantReference\":\"8313842560770001\",\"paymentMethod\":\"visa\",\"pspReference\":\"test_AUTHORISATION_3\",\"reason\":\"REFUSED\",\"success\":\"false\"}}]}"
    val SUCCESS_CALLBACK_RAW_REQUEST =
        "{\"live\":\"false\",\"notificationItems\":[{\"NotificationRequestItem\":{\"additionalData\":{\"cardSummary\":\"7777\",\"eci\":\"N\\/A\",\"shopperIP\":\"127.0.0.1\",\"totalFraudScore\":\"10\",\"hmacSignature\":\"VqB2Yx9xCwJ5eLS8fHvCczTChoyn1Z8ZFMLfKC\\/tM88=\",\"expiryDate\":\"12\\/2012\",\"xid\":\"AAE=\",\"billingAddress.street\":\"Nieuwezijds Voorburgwal\",\"cavvAlgorithm\":\"N\\/A\",\"cardBin\":\"976543\",\"extraCostsValue\":\"101\",\"billingAddress.city\":\"Amsterdam\",\"threeDAuthenticated\":\"false\",\"paymentMethodVariant\":\"visa\",\"billingAddress.country\":\"NL\",\"fraudCheck-6-ShopperIpUsage\":\"10\",\"deviceType\":\"Other\",\" NAME1 \":\"VALUE1\",\"authCode\":\"1234\",\"cardHolderName\":\"J. De Tester\",\"threeDOffered\":\"false\",\"billingAddress.houseNumberOrName\":\"21 - 5\",\"threeDOfferedResponse\":\"N\\/A\",\"NAME2\":\"  VALUE2  \",\"billingAddress.postalCode\":\"1012RC\",\"browserCode\":\"Other\",\"cavv\":\"AAE=\",\"issuerCountry\":\"unknown\",\"threeDAuthenticatedResponse\":\"N\\/A\",\"extraCostsCurrency\":\"EUR\"},\"amount\":{\"currency\":\"EUR\",\"value\":10100},\"eventCode\":\"AUTHORISATION\",\"eventDate\":\"2019-10-09T17:46:08+02:00\",\"merchantAccountCode\":\"Foodpanda_BD\",\"merchantReference\":\"8313842560770001\",\"operations\":[\"CANCEL\",\"CAPTURE\",\"REFUND\"],\"paymentMethod\":\"visa\",\"pspReference\":\"test_AUTHORISATION_1\",\"reason\":\"1234:7777:12\\/2012\",\"success\":\"true\"}}]}"


//        fun createTransactionData(): PaymentGatewayTransactionData {
//            return AdyenTestUtils.createTransactionData(null, true, null, false, null, null, null, null)
//        }
//
//        fun createTransactionData(currency: String): PaymentGatewayTransactionData {
//            return AdyenTestUtils.createTransactionData(currency, true, null, false, null, null, null, null)
//        }
//
//        fun createTransactionDataWithSecurityCode(
//            cardSecurityCode: String,
//            isEncrypted: Boolean
//        ): PaymentGatewayTransactionData {
//            return AdyenTestUtils.createTransactionData(
//                null,
//                false,
//                cardSecurityCode,
//                isEncrypted,
//                null,
//                null,
//                null,
//                null
//            )
//        }

//        fun createTransactionDataWithEncryptedCardData(cardData: String): PaymentGatewayTransactionData {
//            return AdyenTestUtils.createTransactionData(null, false, null, false, cardData, null, null, null)
//        }

//        fun createTransactionDataWithEncryptedCardFields(
//            cardNumber: String,
//            cardExpiryMonth: String,
//            cardExpiryYear: String,
//            cardSecurityCode: String
//        ): PaymentGatewayTransactionData {
//            return AdyenTestUtils.createTransactionData(
//                null,
//                false,
//                cardSecurityCode,
//                true,
//                null,
//                cardNumber,
//                cardExpiryMonth,
//                cardExpiryYear
//            )
//        }
//
//        fun createTransactionData(
//            currency: String?,
//            fakeRawCard: Boolean,
//            cardSecurityCode: String?,
//            cardSecurityCodeIsEncrypted: Boolean,
//            cardData: String?,
//            cardNumber: String?,
//            cardExpiryMonth: String?,
//            cardExpiryYear: String?
//        ): PaymentGatewayTransactionData {
//            val session = PaymentSessionUpdateEx()
//            session.setShouldStorePaymentInstrument(true)
//            session.setBrowserUserAgent("UserAgent")
//            session.setBrowserAcceptHeader("Accept")
//            session.setReturnUrl("http://1.1.1.1")
//            session.setCardHolderName("abc123456")
//            if (fakeRawCard) {
//                session.setRawCardNumber("4111111111111111")
//                session.setRawCardExpiryMonth("03")
//                session.setRawCardExpiryYear("2030")
//                session.setRawCardSecurityCode(SecureString("737"))
//            } else if (cardData != null) {
//                session.setEncryptedCardData(cardData)
//            } else if (cardNumber != null && cardExpiryMonth != null && cardExpiryYear != null) {
//                session.setEncryptedCardNumber(cardNumber)
//                session.setEncryptedExpiryMonth(cardExpiryMonth)
//                session.setEncryptedExpiryYear(cardExpiryYear)
//            }
//            if (cardSecurityCode != null) {
//                if (cardSecurityCodeIsEncrypted) {
//                    session.setEncryptedSecurityCode(SecureString(cardSecurityCode))
//                } else {
//                    session.setRawCardSecurityCode(SecureString(cardSecurityCode))
//                }
//            }
//            session.setUserId(DEFAULT_USER_ID)
//            session.setUserEmail("mathifonseca@gmail.com")
//            session.setUserFirstName("Mathias")
//            session.setUserLastName("Fonseca")
//            session.setUserPhone("099937732")
//            session.setUserIpAddress("1.1.1.1")
//            session.setDeliveryAddressCountry("UY")
//            session.setDeliveryAddressCity("Montevideo")
//            session.setDeliveryAddressState("Cerro Largo")
//            session.setDeliveryAddressStreet("Av. 8 de Octubre")
//            session.setDeliveryAddressDoorNumber("3329")
//            session.setDeliveryAddressPostalCode("11600")
//            session.setPlatformExternalFraudScore("17")
//            val dataProvider = PaymentSessionUpdatePaymentDataProvider.builder().paymentSessionUpdate(session).build()
//            val walletPaymentDataProvider = WalletPaymentDataProvider.forTransaction(PurchaseEx(234L))
//            val paymentDataProviders = List.of<PaymentDataProvider>(dataProvider, walletPaymentDataProvider)
//            val paymentData = PaymentData.builder()
//                .paymentDataProviders(paymentDataProviders)
//                .additionalData(createAdditionalPaymentData())
//                .build()
//            return PaymentGatewayTransactionData.builder()
//                .paymentData(paymentData)
//                .publicTransactionId("123")
//                .amount(Money.fromAmount("10", currency ?: "BDT"))
//                .build()
//        }
//
//        fun createTransactionData(
//            md: String,
//            paRes: String
//        ): PaymentGatewayTransactionData {
//            val session = PaymentSessionUpdateEx()
//            session.setShouldStorePaymentInstrument(true)
//            session.setBrowserUserAgent("UserAgent")
//            session.setBrowserAcceptHeader("Accept")
//            session.setReturnUrl("http://1.1.1.1")
//            session.setCardHolderName("abc123456")
//            session.setUserId(DEFAULT_USER_ID)
//            session.setUserEmail("mathifonseca@gmail.com")
//            session.setUserFirstName("Mathias")
//            session.setUserLastName("Fonseca")
//            session.setUserPhone("099937732")
//            session.setUserIpAddress("1.1.1.1")
//            session.setDeliveryAddressCountry("UY")
//            session.setDeliveryAddressCity("Montevideo")
//            session.setDeliveryAddressState("Cerro Largo")
//            session.setDeliveryAddressStreet("Av. 8 de Octubre")
//            session.setDeliveryAddressDoorNumber("3329")
//            session.setDeliveryAddressPostalCode("11600")
//            session.setMd(md)
//            session.setPaRes(paRes)
//            val dataProvider = PaymentSessionUpdatePaymentDataProvider.builder().paymentSessionUpdate(session).build()
//            val walletPaymentDataProvider = WalletPaymentDataProvider.forTransaction(PurchaseEx(234L))
//            val paymentDataProviders = List.of<PaymentDataProvider>(dataProvider, walletPaymentDataProvider)
//            val paymentData = PaymentData.builder()
//                .paymentDataProviders(paymentDataProviders)
//                .additionalData(createAdditionalPaymentData())
//                .build()
//            return PaymentGatewayTransactionData.builder()
//                .paymentData(paymentData)
//                .publicTransactionId("123")
//                .amount(Money.fromAmount("10", "BDT"))
//                .build()
//        }
//
//        fun createPaymentInstrument(): AbstractPaymentInstrument {
//            return AdyenTestUtils.createPaymentInstrument(null)
//        }

//        fun createPaymentInstrument(token: String?): AbstractPaymentInstrument {
//            val paymentInstrument = CreditCard()
//            val tokens = ArrayList<PaymentInstrumentToken>()
//            tokens.add(
//                PaymentInstrumentToken.builder()
//                    .token(SecureString(token ?: "bbb222"))
//                    .secureStorageIdentifier(AdyenGateway.ADYEN_SECURE_STORAGE_IDENTIFIER)
//                    .build()
//            )
//            paymentInstrument.setExternalToken(tokens)
//            return paymentInstrument
//        }

//        fun createBusinessContract(): BusinessContract {
//            return AdyenTestUtils.createBusinessContract(true)
//        }
//
//        fun createBusinessContract(withValidCredentials: Boolean): BusinessContract {
//            val businessContract = AcquirerContract()
//            val parameters = HashMap<String, BusinessContractParameter>()
//            parameters.put(
//                ADYEN_API_KEY,
//                BusinessContractParameter(ADYEN_API_KEY, if (withValidCredentials) TEST_API_KEY else "apiKey123")
//            )
//            parameters.put(
//                ADYEN_MERCHANT_ACCOUNT,
//                BusinessContractParameter(
//                    ADYEN_MERCHANT_ACCOUNT,
//                    if (withValidCredentials) TEST_MERCHANT_ACCOUNT else "merchantAccount123"
//                )
//            )
//            parameters.put(ADYEN_IS_PRODUCTION, BusinessContractParameter(ADYEN_IS_PRODUCTION, "false"))
//            parameters.put(
//                ADYEN_SHOULD_SEND_BROWSER_INFO,
//                BusinessContractParameter(ADYEN_SHOULD_SEND_BROWSER_INFO, "false")
//            )
//            businessContract.setParameters(parameters)
//            return businessContract
//        }

//


//        fun createAdyenGetCardsRequest(): AdyenGetCardsRequest {
//            val adyenConfigData = createAdyenConfigData()
//            val request = AdyenGetCardsRequest()
//            request.setConfig(adyenConfigData)
//            request.setUserId(DEFAULT_USER_ID)
//            return request
//        }

//        fun createAdyenDisableCardsRequest(): AdyenDisableCardRequest {
//            val adyenConfigData = createAdyenConfigData()
//            val request = AdyenDisableCardRequest()
//            request.setConfig(adyenConfigData)
//            request.setUserId(DEFAULT_USER_ID)
//            request.setToken(DEFAULT_TOKEN)
//            return request
//        }

//    fun createAdyenValidate3DsRequest(): AdyenAuthorizeRequest = AdyenAuthorizeRequest(
//        config = createAdyenConfigData(),
//        amount = DEFAULT_AMOUNT,
//        currency = DEFAULT_CURRENCY,
//        isShouldSendBrowserInfo = false,
//        md = DEFAULT_MD,
//        paRes = DEFAULT_PARES,
//        userId = DEFAULT_USER_ID,
//        isShouldTokenizeCard = true
//    )

    //TODO implement
    fun createValidate3DsRequest(): AuthorizeRequest = throw NotImplementedError()

//        fun createAdyenDisableResult(success: Boolean): DisableResult {
//            val result = DisableResult()
//            result.response = if (success) CARD_SUCCESSFULLY_DISABLED else "error"
//            return result
//        }

//        fun createAdditionalPaymentData(): String? {
//            try {
//                return BaseConverter.objectToJsonString(
//                    AdditionalPaymentData.builder().platformReferenceId(
//                        DEFAULT_PLATFORM_REFERENCE
//                    ).build()
//                )
//            } catch (e: WalletException) {
//                return null
//            }
//
//        }
}
