package com.example.streamline

import com.google.android.gms.tasks.Task
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableReference
import com.google.gson.Gson

val functions = FirebaseFunctions.getInstance()

fun getFun(name: String) : HttpsCallableReference = functions.getHttpsCallable(name)

fun callFun(name: String, params: HashMap<String, Any>) : Task<String> = getFun(name).call(params).continueWith { task -> task.result?.data.toString() }

fun getDict(json: String) : Map<String, String> {
    val gson = Gson()
    val map = gson.fromJson(json, Map::class.java)
    return map as Map<String, String>
}

fun getMapList(list: String) : List<Map<String, String>> {
    val gson = Gson()
    val map = gson.fromJson(list, List::class.java)
    return map as List<Map<String, String>>
}

fun checkIfMixExists(mixName: String) = callFun("check_party", hashMapOf(
    "id" to mixName
))

fun createMix(mixName: String) = callFun("new_party", hashMapOf(
    "id" to mixName
))

fun getFacts(isrc: String) = callFun("get_facts", hashMapOf(
    "isrc" to isrc
))