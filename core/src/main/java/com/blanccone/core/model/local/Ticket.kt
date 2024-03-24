package com.blanccone.core.model.local

data class Ticket(
    var id: String? = "",
    val licenseNumber: String? = "",
    val driverName: String? = "",
    val weight: Int? = 0,
    val weighedOn: String? = "",
    val status: String? = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "licenseNumber" to licenseNumber,
            "driverName" to driverName,
            "weight" to weight,
            "weighedOn" to weighedOn,
            "status" to status
        )
    }
}