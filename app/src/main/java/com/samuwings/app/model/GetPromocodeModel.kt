package com.samuwings.app.model

class GetPromocodeModel {
    private var offer_amount: String? = null

    private var description: String? = null

    private var offer_code: String? = null

    fun getOffer_amount(): String? {
        return offer_amount
    }

    fun setOffer_amount(offer_amount: String?) {
        this.offer_amount = offer_amount
    }

    fun getDescription(): String? {
        return description
    }

    fun setDescription(description: String?) {
        this.description = description
    }

    fun getOffer_code(): String? {
        return offer_code
    }

    fun setOffer_code(offer_code: String?) {
        this.offer_code = offer_code
    }
}