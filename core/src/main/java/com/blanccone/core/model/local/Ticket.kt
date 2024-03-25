package com.blanccone.core.model.local

data class Ticket(
    var id: String? = "",
    val licenseNumber: String? = "",
    val driverName: String? = "",
    val firstWeight: Int? = 0,
    val secondWeight: Int? = 0,
    val firstWeighedOn: String? = "",
    val secondWeighedOn: String? = "",
    val status: String? = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "licenseNumber" to licenseNumber,
            "driverName" to driverName,
            "firstWeight" to firstWeight,
            "secondWeight" to secondWeight,
            "firstWeighedOn" to firstWeighedOn,
            "secondWeighedOn" to secondWeighedOn,
            "status" to status
        )
    }
}