package com.blanccone.persistence.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.blanccone.core.model.local.Ticket
import com.blanccone.core.util.Utils.generateUniqueId

@Entity(tableName = "tb_ticket")
internal data class TicketEntity(
    @PrimaryKey
    var id: String,
    val licenseNumber: String? = "",
    val driverName: String? = "",
    val firstWeight: String? = "",
    val secondWeight: String? = "",
    var nettWeight: String? = "",
    val firstWeighedOn: String? = "",
    val secondWeighedOn: String? = "",
    val status: String? = ""
) {
    companion object {
        fun setEntity(ticket: Ticket): TicketEntity {
            return TicketEntity(
                id = "${ticket.id}",
                licenseNumber = ticket.licenseNumber,
                driverName = ticket.driverName,
                firstWeight = ticket.firstWeight,
                secondWeight = ticket.secondWeight,
                nettWeight = ticket.nettWeight,
                firstWeighedOn = ticket.firstWeighedOn,
                secondWeighedOn = ticket.secondWeighedOn,
                status = ticket.status
            )
        }

        fun setEntityList(tickets: List<Ticket>): List<TicketEntity> {
            val dataList = arrayListOf<TicketEntity>()
            tickets.forEach { ticket ->
                val data = TicketEntity(
                    id = "${ticket.id}",
                    licenseNumber = ticket.licenseNumber,
                    driverName = ticket.driverName,
                    firstWeight = ticket.firstWeight,
                    secondWeight = ticket.secondWeight,
                    firstWeighedOn = ticket.firstWeighedOn,
                    secondWeighedOn = ticket.secondWeighedOn,
                    status = ticket.status
                )
                dataList.add(data)
            }
            return dataList
        }
    }

    fun getEntity() = Ticket(
        id,
        licenseNumber,
        driverName,
        firstWeight,
        secondWeight,
        firstWeighedOn,
        secondWeighedOn,
        status
    )
}
