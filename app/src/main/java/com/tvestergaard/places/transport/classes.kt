package com.tvestergaard.places.transport

data class InSearchResult(
    val id: Int,
    val title: String,
    val description: String,
    val latitude: Float,
    val longitude: Float,
    val pictures: Array<InThumbPicture>
)

data class OutPlace(
    var title: String,
    var description: String,
    var lat: Float,
    var lon: Float,
    var pictures: Array<OutPicture>
)

data class OutPicture(
    var fullData: String?,
    var thumbData: String?
)

data class InThumbPicture(
    var thumbData: String?
)

data class InFullPicture(
    var fullData: String?
)

