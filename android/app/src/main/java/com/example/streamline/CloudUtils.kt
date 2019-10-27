package com.example.streamline

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import com.google.gson.Gson
import java.io.FileNotFoundException
import java.util.concurrent.Callable

const val FUNCTION= "https://us-central1-streamline-5ab87.cloudfunctions.net/"

const val GET_FACTS = "get_facts"
const val CHECK_PARTY = "check_party"
const val NEW_PARTY = "new_party"
const val PLAYLISTS = "playlists"
const val ADD = "add"
const val SAVE = "save"
const val SAVE_GENRE = "gen_playlist"

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

fun getList(list: String) : List<Map<String, String>> {
    val gson = Gson()
    val map = gson.fromJson(list, List::class.java)
    return map as List<Map<String, String>>
}



fun getSongFacts(isrc: String) : Map<String, String> = getDict(callFunction("$FUNCTION$GET_FACTS", mapOf(Pair("isrc", isrc))))

fun checkParty(partyId: String) : Boolean =
    callFunction("$FUNCTION$CHECK_PARTY", mapOf(Pair("id", partyId))) == "True"

fun newParty(partyId: String) : Boolean =
    callFunction("$FUNCTION$NEW_PARTY", mapOf(Pair("id", partyId))) == "Success"

fun playlists(service: String, token: String) : List<Map<String, String>> {
    return getList(callFunction("$FUNCTION$PLAYLISTS", mapOf(Pair("service", service), Pair("token", token))))
}

fun add(id: String, playlist: String, token: String) : Boolean {
    try {
        callFunction(
            "$FUNCTION$ADD",
            mapOf(Pair("id", id), Pair("playlist", playlist), Pair("token", token))
        )
        return true
    } catch (e: FileNotFoundException) {
        return false
    }
}

fun save(partyId: String, name: String, token: String) {
    callFunction("$FUNCTION$SAVE", mapOf(Pair("id", partyId), Pair("name", name), Pair("token", token)))
}

fun saveGenre(partyId: String, name: String, token: String) {
    callFunction("$FUNCTION$SAVE_GENRE", mapOf(Pair("id", partyId), Pair("name", name), Pair("token", token), Pair("numSongs", "5")))
}