package com.blanccone.persistence.service.datasource

import com.blanccone.core.model.local.Ticket
import com.blanccone.core.model.local.WeightImage
import com.blanccone.persistence.AppDatabase
import com.blanccone.persistence.entity.TicketEntity
import com.blanccone.persistence.entity.WeightImageEntity
import javax.inject.Inject

class PersistenceDataSourceImpl @Inject constructor(
    private val appDatabase: AppDatabase
) : PersistenceDataSource {

    override suspend fun insertTicket(ticket: Ticket): Long {
        return appDatabase.appDao().insertTicket(
            TicketEntity.setEntity(ticket)
        )
    }

    override suspend fun getTickets(): List<Ticket> {
        return appDatabase.appDao().getTicketsList()
    }

    override suspend fun insertImage(image: WeightImage): Long {
        return appDatabase.appDao().insertImage(
            WeightImageEntity.setEntity(image)
        )
    }

    override suspend fun getImages(): List<WeightImage> {
        return appDatabase.appDao().getImages()
    }
}