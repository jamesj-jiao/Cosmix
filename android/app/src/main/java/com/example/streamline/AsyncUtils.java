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

    public static List<Playlist> getPlaylists(String service, String token){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<List<Map<String, String>>> future = executor.submit(() -> CloudUtilsKt.playlists(service, token));

        List<Playlist> playlists = new ArrayList<>();

        try {
            List<Map<String, String>> rawPlaylists = future.get();
            for (Map<String, String> playlist : rawPlaylists) {
                String id = playlist.get("id");

                String name = playlist.get("name");
                String image = playlist.get("image");

                playlists.add(new Playlist(id, name == null ? "" : name, image == null ? "" : image));
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return playlists;
    }

    public static boolean add(String id, String playlist, String token) {
        try {
            return Executors.newSingleThreadExecutor().submit(() -> CloudUtilsKt.add(id, playlist, token)).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void save(String id, String name, String token) {
        Executors.newSingleThreadExecutor().execute(() -> CloudUtilsKt.save(id, name, token));
    }
}
