package com.example.streamline

import android.util.Log
import com.google.firebase.database.DataSnapshot
import java.util.concurrent.Callable

const val PARTIES = "parties"
const val ALL_TRACKS = "allTracks"
const val FILTERED_TRACKS = "filtTracks"

fun readSnapshot2(snapshot: DataSnapshot) : Set<Song> = AsyncUtils.getSongs(snapshot.getValue(List::class.java) as List<String>)

fun readSnapshot(snapshot: DataSnapshot) :Set<Song> {
    Log.wtf("AAAAAAAAAAAAAAAA", "$snapshot")
    for (child in snapshot.children) {
        Log.wtf("AAAAAAAAAAAAAAAA", "$child")
    }
    return setOf()
}