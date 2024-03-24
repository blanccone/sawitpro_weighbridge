package com.blanccone.persistence.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.blanccone.core.model.local.Ticket
import com.blanccone.core.model.local.WeightImage

@Entity(tableName = "tb_weight_image")
internal data class WeightImageEntity(
    @PrimaryKey
    var id: String,
    var ticketId: String? = "",
    var image: ByteArray,
    var imageName: String? = "",
    var imagePath: String? = ""
) {
    companion object {
        fun setEntity(image: WeightImage): WeightImageEntity {
            return WeightImageEntity(
                id = "${image.id}",
                ticketId = image.ticketId,
                image = image.image,
                imageName = image.imageName,
                imagePath = image.imagePath
            )
        }
    }

    fun getEntity() = WeightImage(
        id,
        ticketId,
        image,
        imageName,
        imagePath
    )
}
