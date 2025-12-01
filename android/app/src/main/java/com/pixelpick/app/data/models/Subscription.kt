package com.pixelpick.app.data.models

import com.google.gson.annotations.SerializedName

data class Subscription(
    @SerializedName("id")
    val id: Int? = null,
    
    @SerializedName("user_id")
    val userId: Int? = null,
    
    @SerializedName("plan_type")
    val planType: String? = null,
    
    @SerializedName("amount")
    val amount: Double? = null,
    
    @SerializedName("currency")
    val currency: String? = null,
    
    @SerializedName("status")
    val status: String? = null, // 'active', 'cancelled', 'expired'
    
    @SerializedName("current_period_start")
    val currentPeriodStart: String? = null,
    
    @SerializedName("current_period_end")
    val currentPeriodEnd: String? = null,
    
    @SerializedName("cancel_at_period_end")
    val cancelAtPeriodEnd: Boolean? = null,
    
    @SerializedName("has_premium_access")
    val hasPremiumAccess: Boolean? = null
)

data class SubscriptionStatusResponse(
    @SerializedName("has_subscription")
    val hasSubscription: Boolean,
    
    @SerializedName("subscription")
    val subscription: Subscription? = null
)

data class PaymentIntentResponse(
    @SerializedName("client_secret")
    val clientSecret: String,
    
    @SerializedName("payment_intent_id")
    val paymentIntentId: String
)

