package app.analytics.wrapped;

import app.audio.Album;
import app.audio.Song;
import app.users.Artist;
import app.users.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;

public final class ArtistStats implements Wrapped {
    @Getter
    private final HashMap<User, Integer> fans = new HashMap<>();
    private final Artist artist;
    @Getter
    private final ArrayList<Album> removedAlbums = new ArrayList<>();


    public ArtistStats(final Artist artist) {
        this.artist = artist;
    }

    /**
     * Method called from UserStats when user listens a song from artist.
     * @param user the fan
     */

    public void addFan(final User user) {
        fans.put(user, fans.getOrDefault(user, 0) + 1);
    }

    /**
     * Print statistics about artist. Format:
     * topAlbums, topSongs, topFans, listeners
     *
     * @param objectNode for output
     */
    @Override
    public void wrapped(final ObjectNode objectNode) {
        if (isEmpty()) {
            objectNode.put("message", "No data to show for artist " + artist.getUsername() + ".");
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode1 = objectMapper.createObjectNode();
        ObjectNode objectNode2 = objectMapper.createObjectNode();

        ArrayList<Album> allAlbums = new ArrayList<>(artist.getAlbums());
        allAlbums.addAll(removedAlbums);
        HashMap<Album, Integer> uniqueAlbums = Wrapped.mergeDuplicateAlbums(
                Wrapped.createHashMapFromArrayList(allAlbums));
        LinkedHashMap<Album, Integer> albumsResults = Wrapped.createResults(uniqueAlbums,
                ALBUM_COMPARATOR);
        for (Album album : albumsResults.keySet()) {
            objectNode2.put(album.getName(), albumsResults.get(album));
        }
        objectNode1.set("topAlbums", objectNode2);

        objectNode2 = objectMapper.createObjectNode();
        HashMap<Song, Integer> allSongsHashMap = new HashMap<>();
        for (Album album : allAlbums) {
            Wrapped.mergeMaps(allSongsHashMap,
                    Wrapped.createHashMapFromArrayList(album.getSongs()));
        }
        HashMap<Song, Integer> uniqueSongs = Wrapped.mergeDuplicateSongs(allSongsHashMap);
        LinkedHashMap<Song, Integer> songsResults = Wrapped.createResults(
                uniqueSongs, SONG_COMPARATOR);
        for (Song song : songsResults.keySet()) {
            objectNode2.put(song.getName(), songsResults.get(song));
        }
        objectNode1.set("topSongs", objectNode2);

        LinkedHashMap<User, Integer> fansResults = Wrapped.createResults(
                fans, USER_COMPARATOR);
        ArrayNode arrayNode = objectMapper.createArrayNode();
        for (User fan : fansResults.keySet()) {
            arrayNode.add(fan.getUsername());
        }
        objectNode1.set("topFans", arrayNode);

        objectNode1.put("listeners", fans.size());


        objectNode.set("result", objectNode1);
    }

    /**
     * Get top 5 fans by number of listened songs from artist.
     * @return ArrayList of users
     */
    public ArrayList<User> getTop5Fans() {
        LinkedHashMap<User, Integer> fansResults = Wrapped.createResults(fans, USER_COMPARATOR);
        return new ArrayList<>(fansResults.keySet());
    }

    @Override
    public boolean isEmpty() {
        return fans.isEmpty();
    }


}
