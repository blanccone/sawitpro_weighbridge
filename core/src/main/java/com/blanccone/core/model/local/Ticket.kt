package com.blanccone.core.model.local

data class Ticket(
    var id: String? = "",
    var licenseNumber: String? = "",
    var driverName: String? = "",
    var firstWeight: String? = "",
    var secondWeight: String? = "",
    var firstWeighedOn: String? = "",
    var secondWeighedOn: String? = "",
    var status: String? = ""
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