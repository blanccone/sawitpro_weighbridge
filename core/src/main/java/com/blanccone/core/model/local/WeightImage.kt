package com.blanccone.core.model.local

data class WeightImage(
    var id: String? = "",
    var ticketId: String? = "",
    var image: ByteArray?,
    var imageName: String? = "",
    var imagePath: String? = "",
)
