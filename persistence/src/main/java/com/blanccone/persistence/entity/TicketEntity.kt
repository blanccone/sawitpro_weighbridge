package com.blanccone.persistence.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.blanccone.core.model.local.Ticket
import com.blanccone.core.util.Utils.generateUniqueId

@Entity(tableName = "tb_ticket")
internal data class TicketEntity(
    @PrimaryKey
    var id: String? = "",
    val licenseNumber: String? = "",
    val driverName: String? = "",
    val weight: Int? = 0,
    val weighedOn: String? = "",
    val status: String? = ""
) {
    companion object {
        fun setEntity(ticket: Ticket): TicketEntity {
            return TicketEntity(
                id = ticket.id,
                licenseNumber = ticket.licenseNumber,
                driverName = ticket.driverName,
                weight = ticket.weight,
                weighedOn = ticket.weighedOn,
                status = ticket.status
            )
        }
    }

    fun getEntity() = Ticket(
        id,
        licenseNumber,
        driverName,
        weight,
        weighedOn,
        status
    )
}
