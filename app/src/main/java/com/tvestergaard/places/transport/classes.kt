package com.tvestergaard.places.transport

data class SearchResult(
    val id: Int,
    val title: String,
    val desc: String,
    val latitude: Float,
    val longitude: Float
)