package app.analytics.wrapped;

import app.audio.Song;
import app.audio.Album;
import app.audio.Episode;
import app.audio.AudioObject;
import app.users.User;
import app.utils.MyConst;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

public interface Wrapped {
    /**
     * Print relevant statistics about users activity.
     * @param objectNode for output
     */
    void wrapped(ObjectNode objectNode);

    /**
     * Check if a user has something in his statistics.
     * @return true if user played nothing before
     */
    boolean isEmpty();

    Comparator<Map.Entry<Song, Integer>> SONG_COMPARATOR = Comparator
            .comparing(Map.Entry<Song, Integer>::getValue, Comparator.reverseOrder())
            .thenComparing(entry -> entry.getKey().getName());
    Comparator<Map.Entry<Album, Integer>> ALBUM_COMPARATOR = Comparator
            .comparing(Map.Entry<Album, Integer>::getValue, Comparator.reverseOrder())
            .thenComparing(entry -> entry.getKey().getName());
    Comparator<Map.Entry<Episode, Integer>> EPISODE_COMPARATOR = Comparator
            .comparing(Map.Entry<Episode, Integer>::getValue, Comparator.reverseOrder())
            .thenComparing(entry -> entry.getKey().getName());
    Comparator<Map.Entry<String, Integer>> NAME_COMPARATOR = Comparator
            .comparing(Map.Entry<String, Integer>::getValue, Comparator.reverseOrder())
            .thenComparing(Map.Entry::getKey);
    Comparator<Map.Entry<User, Integer>> USER_COMPARATOR = Comparator
            .comparing(Map.Entry<User, Integer>::getValue, Comparator.reverseOrder())
            .thenComparing(entry -> entry.getKey().getUsername());

    /**
     * Sort a HashMap with Integer values by a given comparator, selecting maximum 5 elements.
     * @param map the map to be sorted
     * @param comparator the comparator used
     * @return a LinkedHashMap with results (to make sure the order is correct)
     * @param <K> any type is supported, but it's mostly used with AudioObject and String
     */

    static <K> LinkedHashMap<K, Integer> createResults(
            HashMap<K, Integer> map, Comparator<Map.Entry<K, Integer>> comparator) {
        return map.entrySet()
                .stream()
                .sorted(comparator)
                .limit(MyConst.RESULT_SIZE)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    /**
     * Create HashMap from ArrayList of audio objects, taking listened as value.
     * @param arrayList the list of objects
     * @return HashMap of object-listens key-value pairs
     * @param <K> any class that extends AudioObject (for method getListened)
     */
    static <K extends AudioObject> HashMap<K, Integer> createHashMapFromArrayList(
            ArrayList<K> arrayList) {
        HashMap<K, Integer> hashMap = new HashMap<>();
        for (K elem : arrayList) {
            if (elem.getListened() > 0) {
                hashMap.put(elem, elem.getListened());
            }
        }
        return hashMap;
    }

    /**
     * Merge two HashMaps summing their values for same key.
     * @param map1 first and result map
     * @param map2 the second map
     * @param <K> any AudioObject type
     */
    static <K extends AudioObject> void mergeMaps(HashMap<K, Integer> map1,
                                                  HashMap<K, Integer> map2) {
        for (K elem : map2.keySet()) {
            map1.put(elem, map1.getOrDefault(elem, 0) + map2.get(elem));
        }
    }

    /**
     * Merge two songs with same name and same artist in one.
     * The new map contains the sum of their listens.
     * @param songs a map with duplicated songs
     * @return new map without duplicates
     */
    static HashMap<Song, Integer> mergeDuplicateSongs(HashMap<Song, Integer> songs) {
        HashMap<Song, Integer> result = new HashMap<>();
        for (Song song : songs.keySet()) {
            Song song1 = containsSong(result, song);
            if (song1 != null) {
                result.put(song1, result.get(song1) + songs.get(song));
            } else {
                result.put(song, songs.get(song));
            }
        }
        return result;
    }
    /**
     * Merge two albums with same name in one.
     * The new map contains the sum of their listens.
     * @param albums a map with duplicated albums
     * @return new map without duplicates
     */
    static HashMap<Album, Integer> mergeDuplicateAlbums(HashMap<Album, Integer> albums) {
        HashMap<Album, Integer> result = new HashMap<>();
        for (Album album : albums.keySet()) {
            Album album1 = containsAlbum(result, album);
            if (album1 != null) {
                result.put(album1, result.get(album1) + albums.get(album));
            } else {
                result.put(album, albums.get(album));
            }
        }
        return result;
    }

    private static Song containsSong(HashMap<Song, Integer> songs, Song song) {
        for (Song song1 : songs.keySet()) {
            if (song1.getName().equals(song.getName())
                    && song1.getArtist().equals(song.getArtist())) {
                return song1;
            }
        }
        return null;
    }

    private static Album containsAlbum(HashMap<Album, Integer> albums, Album album) {
        for (Album album1 : albums.keySet()) {
            if (album1.getName().equals(album.getName())) {
                return album1;
            }
        }
        return null;
    }
}
