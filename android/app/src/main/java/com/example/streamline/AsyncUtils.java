package com.example.streamline;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsyncUtils {
    public static Set<Song> getSongs(List<String> ids) {
        ExecutorService executor = Executors.newCachedThreadPool();

        List<Future<Song>> futures = new ArrayList<>();

        for (String isrc : ids) {
            futures.add(executor.submit(() -> {
                Map<String, String> songDict = CloudUtilsKt.getSongFacts(isrc);

                return new Song(isrc, songDict.get("name"), songDict.get("artist"));
            }));
        }

        Set<Song> songs = new HashSet<>();

        for (Future<Song> future : futures) {
            try {
                songs.add(future.get());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        return songs;
    }

    public static boolean checkParty(String partyID) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(() -> CloudUtilsKt.checkParty(partyID));
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void newParty(String partyID) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(() -> CloudUtilsKt.newParty(partyID));
        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
