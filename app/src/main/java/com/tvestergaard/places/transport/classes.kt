package com.tvestergaard.places.transport

data class InSearchResult(
    val id: Int,
    val title: String,
    val description: String,
    val latitude: Float,
    val longitude: Float,
    val pictures: Array<InPicture>,
    var user: InUser
)

data class OutPlace(
    var title: String,
    var description: String,
    var lat: Float,
    var lon: Float,
    var pictures: Array<OutPicture>
)

data class OutPicture(
    var fullData: String,
    var thumbData: String
)

data class InPicture(
    var thumbName: String,
    var fullName: String
)

data class InUser(
    var id: Int,
    var name: String
)

