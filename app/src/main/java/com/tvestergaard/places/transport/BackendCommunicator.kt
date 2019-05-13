package com.tvestergaard.places.transport

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.toolbox.HttpClient
import com.github.kittinunf.result.Result
import com.google.gson.GsonBuilder
import java.net.URI
import java.net.URL

class BackendCommunicator {

    val gson = GsonBuilder().create();

    fun search(term: String): List<SearchResult> {

//        var results: List<SearchResult>? = null
//        val response = run {
//            SEARCH_URL.httpGet()
//                .responseString { req, res, result ->
//                    when (result) {
//                        is Result.Failure -> {
//                            throw result.getException()
//                        }
//                        is Result.Success -> {
//                            results = gson.fromJson(result.value, Array<SearchResult>::class.java).toList()
//                        }
//                    }
//                }
//        }
//
//        routine.join()
//
//        return results!!

        return (gson.fromJson(URL(SEARCH_URL).readText(), Array<SearchResult>::class.java).toList())
    }

    companion object {
        private const val SEARCH_URL = "http://3314d9e7.ngrok.io/search/greenland";
    }
}