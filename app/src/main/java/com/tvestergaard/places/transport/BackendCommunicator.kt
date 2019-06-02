package com.tvestergaard.places.transport

import com.google.gson.GsonBuilder
import com.tvestergaard.places.pages.AuthenticatedUser
import khttp.get
import khttp.post

class BackendCommunicator {

    val gson = GsonBuilder().create();

    fun authenticateWithBackend(token: String?): AuthenticatedUser? {
        if (token == null)
            return token

        val url = "$ROOT/authenticate/google/$token"
        val response = post(url)
        if (!ok(response.statusCode))
            return null

        return gson.fromJson(response.text, AuthenticatedUser::class.java)
    }

    fun search(term: String): List<InSearchResult> {
        val url = "$ROOT/places/search/$term"
        val response = get(url)
        if (!ok(response.statusCode))
            return listOf()

        return gson.fromJson(response.text, Array<InSearchResult>::class.java).toList()
    }

    private fun ok(code: Int) = code in 200..299

    fun postPlace(OutPlace: OutPlace): OutPlace? {
        val response = post("$ROOT/places", data = com.tvestergaard.places.fragments.gson.toJson(OutPlace), headers = mapOf("Content-Type" to "application/json",
                                                                                                    "Authorization" to "Bearer " + authenticatedUser!!.token))
        if (!ok(response.statusCode))
            return null
        return gson.fromJson(response.text, OutPlace::class.java)

    }

    companion object {
        private const val ROOT = "http://b4a6c734.ngrok.io";
        var authenticatedUser: AuthenticatedUser? = null
        const val IMG_ROOT = "$ROOT/images"
    }
}