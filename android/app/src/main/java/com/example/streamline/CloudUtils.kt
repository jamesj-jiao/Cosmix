package com.example.streamline

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import com.google.gson.Gson
import java.util.concurrent.Callable

const val FUNCTION= "https://us-central1-streamline-5ab87.cloudfunctions.net/"

const val GET_FACTS = "get_facts"
const val CHECK_PARTY = "check_party"
const val NEW_PARTY = "new_party"

fun callFunction(url: String, params: Map<String, String>) : String {

    var formatted = "$url?"

    for ((param, value) in params) {
        formatted = formatted.plus("&$param=$value")
    }

    var response = ""

    with(URL(formatted).openConnection() as HttpURLConnection) {
        requestMethod = "POST"

        inputStream.bufferedReader().use {
            it.lines().forEach { line ->
                response = response.plus(line)
            }
        }
    }

    return response
}

fun getDict(json: String) : Map<String, String> {
    val gson = Gson()
    val map = gson.fromJson(json, Map::class.java)
    return map as Map<String, String>
}

fun getSongFacts(isrc: String) : Map<String, String> = getDict(callFunction("$FUNCTION$GET_FACTS", mapOf(Pair("isrc", isrc))))

fun checkParty(partyId: String) : Boolean =
    callFunction("$FUNCTION$CHECK_PARTY", mapOf(Pair("id", partyId))) == "True"

fun newParty(partyId: String) : Boolean =
    callFunction("$FUNCTION$NEW_PARTY", mapOf(Pair("id", partyId))) == "Success"