# Introduction

## What is Alfred?

Online payments are an essential feature when we want to provide the best ordering experience to our users. That's why having a solid foundation for all the payment related topics in Delivery Hero is also essential.

Alfred is a Global Service developed by the central team in Delivery Hero that provides that foundation for every Delivery Hero entity. Its purpose is to add more value to the already existing payment and fraud prevention solutions developed by the entities. This is done by adding more features (like the wallet management, card vaulting, and KYC) and by improving the existing third-party integrations (like reducing the transaction fees by approaching the third-parties as DH as a whole and not just one entity; or unifying the technical integrations into one, so that we reduce the effort of maintaining them).

> [TODO insert diagram of current entities integrations and Alfred's future]

## What is a PSP Connector?

Alfred's core services live in a Java Spring application that exposes the features that are used by the entities. As an example, if a Pandora user wants to pay for an order using PayPal, the processing would be something like:

> [TODO insert diagram of the flow]

- Pandora checks that the order data is valid (the restaurant is open, delivers to the selected address, etc.)
- Pandora calls Alfred to process the purchase with the needed information (could be a stored credit card or a new one, could be paid in part with wallet balance, etc.)
- Alfred decides how to process the payment depending on several parameters. For example, if the selected payment instrument is a stored credit card that was tokenized by a specific PSP, we need to perform the payment in that same PSP.
- Once the PSP is chosen, Alfred needs to communicate with the PSP to authorize the payment. This communication varies a lot from one PSP to the other. It can be a SOAP or REST web service, it may have one type of request signature or none at all, it may require different data points with varying names although they mean the same, and a huge etcétera.
- After calling the PSP for authorizing the payment, Alfred needs to be able to parse the response and interpret the response codes, mapping each of them to a generic enumeration of possible outcomes of a transaction. For example, some PSPs indicate that the transaction was successful by setting as `true` a `boolean` property in the response. However, other PSPs will throw an exception through their SDKs, others will set to `Success` a `String` property, and even have a numeric response field indicating the result (which can be 0, -1 or 234), there is no standard!

Given the great variability among the PSP integrations, we need to be very mindful of the **modifiability** of our code, because it's going to change often and sometimes urgently (imagine what happens if a PSP unexpectedly changes the response code meanings from one day to the other). And if you add **availability** on that mix (being able to detect and react when a PSP is underperforming or completely down), the payments module starts looking like a living organism that lives inside Alfred.

For these reasons, the solution we propose is that **each PSP integration lives in its scope and the changes related to that specific PSP do not affect in any way the rest of the Alfred platform or other PSP integrations**. We call these PSP integration modules, the **Connectors**. In an ideal scenario, the Connectors should behave as an external service from Alfred's perspective, so that we can separately manage their release cycle and we reduce the scope of a disaster if something goes wrong.

Apart from the technical advantages of keeping these PSP integration modules separate, we can also take advantage of them from the team's perspective. If we create a team dedicated to creating and maintaining the third-party integrations, that team can really focus on optimizing every detail of the integrations, reacting fast to changes, releasing new integrations that can be used with minimal changes from the entities (because the entities shouldn't care of how Alfred resolves the payment logic), etc. 

Even more, with this in mind, we can create a module that acts as a nexus between Alfred and every existing PSP. So Alfred can always send the requests in a predefined format and get the responses in the same predefined format, being unaware of the internal wiring for each PSP. We call this module the **Connector SDK**.

> [TODO insert diagram of the connector's future]

The Connector SDK should then contain everything we need to avoid Alfred to know about the PSPs. Mainly, we want to avoid the mapping from Alfred to every possible PSP specific requests and responses. Therefore, we defined a "dialect" that Alfred speaks with the Connector SDK and then leave the specific mapping to each Connector.

So, the responsibilities of the SDK are:

- Operation requests mapping
- Operation response mapping
- Request validation
- Error handling
- Generic logging
