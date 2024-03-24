package com.blanccone.persistence.service.datasource

import com.blanccone.core.model.local.Ticket
import com.blanccone.core.model.local.WeightImage

interface PersistenceDataSource {

    suspend fun insertTicket(ticket: Ticket): Long

    suspend fun getTickets(): List<Ticket>

    suspend fun insertImage(image: WeightImage): Long

    suspend fun getImages(): List<WeightImage>
}