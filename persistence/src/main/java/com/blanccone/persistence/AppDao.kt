package com.blanccone.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.blanccone.core.model.local.Ticket
import com.blanccone.core.model.local.WeightImage
import com.blanccone.persistence.entity.TicketEntity
import com.blanccone.persistence.entity.WeightImageEntity

@Dao
internal interface AppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<TicketEntity>): List<Long>

    @Transaction
    @Query("SELECT * FROM tb_ticket")
    suspend fun getTicketsList(): List<Ticket>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: WeightImageEntity): Long

    @Transaction
    @Query("SELECT * FROM tb_weight_image WHERE ticketId == :ticketId")
    suspend fun getImages(ticketId: String): List<WeightImage>

    @Update
    suspend fun updateTicket(ticket: TicketEntity) : Int

    @Update
    suspend fun updateImage(image: WeightImageEntity) : Int
}