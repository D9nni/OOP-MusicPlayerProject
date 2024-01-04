package app.analytics.wrapped;

import app.audio.Album;
import app.audio.Episode;
import app.audio.Library;
import app.audio.Song;
import app.users.User;
import app.utils.MyConst;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public interface Wrapped {
    void wrapped(ObjectNode objectNode);
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
}
