package io.specmatic.async.model

import kotlinx.serialization.Serializable

@Serializable
sealed class OrderEvent {
    abstract val eventType: String
    abstract val timestamp: String
}

@Serializable
data class PlaceOrderEvent(
    override val eventType: String = "place-order-event",
    override val timestamp: String,
    val orderId: String,
    val customerId: String,
    val items: List<OrderItem>,
    val totalAmount: Double
) : OrderEvent()

@Serializable
data class CancelOrderEvent(
    override val eventType: String = "cancel-order-event",
    override val timestamp: String,
    val orderId: String,
    val customerId: String,
    val reason: String
) : OrderEvent()

@Serializable
data class OrderItem(
    val productId: String,
    val quantity: Int,
    val price: Double
)

