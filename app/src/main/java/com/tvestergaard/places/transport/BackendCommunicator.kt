package com.tvestergaard.places.transport

import com.google.gson.GsonBuilder
import com.tvestergaard.places.pages.AuthenticatedUser
import khttp.get
import khttp.post
import java.net.URL

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

    fun search(term: String): List<SearchResult> {
        val url = "$ROOT/places/search/$term"
        val response = get(url)
        if (!ok(response.statusCode))
            return listOf()

        return gson.fromJson(response.text, Array<SearchResult>::class.java).toList()
    }

    private fun ok(code: Int) = code in 200..299

    companion object {
        private const val ROOT = "http://b4a6c734.ngrok.io";
    }
}