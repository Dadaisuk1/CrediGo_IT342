package com.sia.credigo.utils

import com.sia.credigo.model.PaymentResponse
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

/**
 * Custom JSON deserializer for PaymentResponse class
 * Handles different formats of payment responses from the API
 */
class PaymentResponseConverter : JsonDeserializer<PaymentResponse> {
    
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): PaymentResponse {
        val jsonObject = json.asJsonObject
        
        // Extract fields using various possible field names
        val clientSecret = extractString(jsonObject, listOf("clientSecret", "client_secret"))
        val checkoutUrl = extractString(jsonObject, listOf("checkoutUrl", "checkout_url"))
        val message = extractString(jsonObject, listOf("message", "msg"))
        
        return PaymentResponse(
            clientSecret = clientSecret,
            checkoutUrl = checkoutUrl,
            message = message
        )
    }
    
    /**
     * Extract a string from a JsonObject using a list of possible field names
     */
    private fun extractString(jsonObject: JsonObject, fieldNames: List<String>): String? {
        for (fieldName in fieldNames) {
            if (jsonObject.has(fieldName) && !jsonObject.get(fieldName).isJsonNull) {
                return jsonObject.get(fieldName).asString
            }
        }
        return null
    }
} 