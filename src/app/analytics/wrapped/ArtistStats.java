package app.analytics.wrapped;

import app.audio.Album;
import app.audio.AudioObject;
import app.audio.Song;
import app.users.Artist;
import app.users.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;
import java.util.stream.Collectors;

public class ArtistStats implements Wrapped {
    private final HashMap<User, Integer> fans = new HashMap<>();
    private final Artist artist;


    public ArtistStats(Artist artist) {
        this.artist = artist;
    }

    public void addFan(User user) {
        fans.put(user, fans.getOrDefault(user, 0) + 1);
    }
    @Override
    public void wrapped(ObjectNode objectNode) {
        //topAlbums, topSongs, topFans, listeners
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode1 = objectMapper.createObjectNode();
        ObjectNode objectNode2 = objectMapper.createObjectNode();

        LinkedHashMap<Album, Integer> albumsResults = Wrapped.createResults(
                createHashMapFromArrayList(artist.getAlbums()), albumComparator);
        for (Album album : albumsResults.keySet()) {
            objectNode2.put(album.getName(), albumsResults.get(album));
        }
        objectNode1.set("topAlbums", objectNode2);

        objectNode2 = objectMapper.createObjectNode();
        HashMap<Song, Integer> allSongsHashMap = new HashMap<>();
        for (Album album : artist.getAlbums()) {
            mergeMaps(allSongsHashMap, createHashMapFromArrayList(album.getSongs()));
        }
        LinkedHashMap<Song, Integer> songsResults = Wrapped.createResults(allSongsHashMap, songComparator);
        for (Song song : songsResults.keySet()) {
            objectNode2.put(song.getName(), songsResults.get(song));
        }
        objectNode1.set("topSongs", objectNode2);

        LinkedHashMap<User, Integer> fansResults = Wrapped.createResults(
                fans, userComparator);
        ArrayNode arrayNode = objectMapper.createArrayNode();
        for (User fan : fansResults.keySet()) {
            arrayNode.add(fan.getUsername());
        }
        objectNode1.set("topFans", arrayNode);

        objectNode1.put("listeners", fans.size());


        objectNode.set("result", objectNode1);
    }

    private static <K extends AudioObject> HashMap<K, Integer> createHashMapFromArrayList(ArrayList<K> arrayList) {
        HashMap<K, Integer> hashMap = new HashMap<>();
        for (K elem : arrayList) {
            if(elem.getListened() > 0) {
                hashMap.put(elem, elem.getListened());
            }
        }
        return hashMap;
    }

    private static <K extends AudioObject> void mergeMaps(HashMap<K, Integer> map1, HashMap<K, Integer> map2) {
        for (K elem : map2.keySet()) {
            map1.put(elem, map1.getOrDefault(elem, 0) + map2.get(elem));
        }
    }
    public boolean hasFans() {
        return !fans.isEmpty();
    }

}
