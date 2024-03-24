package com.blanccone.persistence.service.repository

import com.blanccone.core.model.local.Ticket
import com.blanccone.core.model.local.WeightImage
import com.blanccone.persistence.service.datasource.PersistenceDataSource
import com.technicaltest.core.service.util.DatabaseBoundSource
import com.technicaltest.core.service.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class PersistenceRepository @Inject constructor(
    private val dataSource: PersistenceDataSource
) {

    fun insertTicket(ticket: Ticket): Flow<Resource<Long>> =
        object : DatabaseBoundSource<Long, Long>(QUERY_INSERT_SINGLE) {
            override suspend fun fetchFromLocal(): Long {
                return dataSource.insertTicket(ticket)
            }

            override suspend fun postProcess(originalData: Long): Long {
                return originalData
            }
        }.asFlow().flowOn(Dispatchers.IO)

    fun getTickets(): Flow<Resource<List<Ticket>>> =
        object : DatabaseBoundSource<List<Ticket>, List<Ticket>>(QUERY_SELECT_MULTIPLE) {
            override suspend fun fetchFromLocal(): List<Ticket> {
                return dataSource.getTickets()
            }

            override suspend fun postProcess(originalData: List<Ticket>): List<Ticket> {
                return originalData
            }
        }.asFlow().flowOn(Dispatchers.IO)

    fun insertImage(image: WeightImage): Flow<Resource<Long>> =
        object : DatabaseBoundSource<Long, Long>(QUERY_INSERT_SINGLE) {
            override suspend fun fetchFromLocal(): Long {
                return dataSource.insertImage(image)
            }

            override suspend fun postProcess(originalData: Long): Long {
                return originalData
            }
        }.asFlow().flowOn(Dispatchers.IO)

    fun getImages(): Flow<Resource<List<WeightImage>>> =
        object : DatabaseBoundSource<List<WeightImage>, List<WeightImage>>(QUERY_SELECT_MULTIPLE) {
            override suspend fun fetchFromLocal(): List<WeightImage> {
                return dataSource.getImages()
            }

            override suspend fun postProcess(originalData: List<WeightImage>): List<WeightImage> {
                return originalData
            }
        }.asFlow().flowOn(Dispatchers.IO)
}