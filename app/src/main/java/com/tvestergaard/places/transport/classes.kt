package com.tvestergaard.places.transport

import java.io.Serializable

data class InPlace(
    val id: Int,
    val title: String,
    val description: String,
    val latitude: Float,
    val longitude: Float,
    val pictures: Array<InPicture>,
    var user: InUser
) : Serializable

data class OutPlace(
    var title: String,
    var description: String,
    var latitude: Float,
    var longitude: Float,
    var pictures: Array<OutPicture>
)

data class OutPicture(
    var fullData: String,
    var thumbData: String
)

data class InPicture(
    var thumbName: String,
    var fullName: String
) : Serializable

data class InUser(
    var id: Int,
    var name: String
) : Serializable

