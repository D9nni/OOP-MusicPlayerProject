package app.analytics.wrapped;

import app.audio.*;
import app.users.Artist;
import app.users.User;
import app.utils.MyConst;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;
import java.util.stream.Collectors;

public interface Wrapped {
    void wrapped(ObjectNode objectNode);

    boolean isEmpty();

    Comparator<Map.Entry<Song, Integer>> songComparator = Comparator
            .comparing(Map.Entry<Song, Integer>::getValue, Comparator.reverseOrder())
            .thenComparing(entry -> entry.getKey().getName());
    Comparator<Map.Entry<String, Integer>> artistComparator = Comparator
            .comparing(Map.Entry<String, Integer>::getValue, Comparator.reverseOrder())
            .thenComparing(Map.Entry::getKey);
    Comparator<Map.Entry<Album, Integer>> albumComparator = Comparator
            .comparing(Map.Entry<Album, Integer>::getValue, Comparator.reverseOrder())
            .thenComparing(entry -> entry.getKey().getName());
    Comparator<Map.Entry<Episode, Integer>> episodeComparator = Comparator
            .comparing(Map.Entry<Episode, Integer>::getValue, Comparator.reverseOrder())
            .thenComparing(entry -> entry.getKey().getName());
    Comparator<Map.Entry<String, Integer>> genreComparator = Comparator
            .comparing(Map.Entry<String, Integer>::getValue, Comparator.reverseOrder())
            .thenComparing(Map.Entry::getKey);
    Comparator<Map.Entry<User, Integer>> userComparator = Comparator
            .comparing(Map.Entry<User, Integer>::getValue, Comparator.reverseOrder())
            .thenComparing(entry -> entry.getKey().getUsername());

    static <K> LinkedHashMap<K, Integer> createResults(HashMap<K, Integer> map, java.util.Comparator<Map.Entry<K, Integer>> comparator) {
        return map.entrySet()
                .stream()
                .sorted(comparator)
                .limit(MyConst.RESULT_SIZE)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    static <K extends AudioObject> HashMap<K, Integer> createHashMapFromArrayList(ArrayList<K> arrayList) {
        HashMap<K, Integer> hashMap = new HashMap<>();
        for (K elem : arrayList) {
            if (elem.getListened() > 0) {
                hashMap.put(elem, elem.getListened());
            }
        }
        return hashMap;
    }

    static <K extends AudioObject> void mergeMaps(HashMap<K, Integer> map1, HashMap<K, Integer> map2) {
        for (K elem : map2.keySet()) {
            map1.put(elem, map1.getOrDefault(elem, 0) + map2.get(elem));
        }
    }

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
            if (song1.getName().equals(song.getName()) && song1.getArtist().equals(song.getArtist())) {
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
