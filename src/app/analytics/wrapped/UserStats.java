package app.analytics.wrapped;

import app.audio.Song;
import app.audio.Library;
import app.audio.Album;
import app.audio.Episode;
import app.audio.AudioFile;
import app.audio.AudioObject;
import app.audio.Podcast;
import app.users.Admin;
import app.users.Artist;
import app.users.Host;
import app.users.User;
import app.utils.MyConst;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedHashMap;

public final class UserStats implements Wrapped {
    @Getter
    private final HashMap<Song, Integer> songs = new HashMap<>();
    @Getter//artist may not exist as instance
    private final HashMap<String, Integer> artists = new HashMap<>();
    private final HashMap<Album, Integer> albums = new HashMap<>();
    private final HashMap<Episode, Integer> episodes = new HashMap<>();
    private final HashMap<String, Integer> genres = new HashMap<>();
    private final User user;

    public UserStats(final User user) {
        this.user = user;
    }

    /**
     * Print statistics about user's preferences. Format:
     * topArtists, topGenres, topSongs, topAlbums, topEpisodes
     * @param objectNode for output
     */
    @Override
    public void wrapped(final ObjectNode objectNode) {
        if (isEmpty()) {
            objectNode.put("message",
                    "No data to show for user " + user.getUsername() + ".");
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode1 = objectMapper.createObjectNode();
        ObjectNode objectNode2 = objectMapper.createObjectNode();
        LinkedHashMap<String, Integer> resultArtists = Wrapped
                .createResults(artists, NAME_COMPARATOR);
        for (String artistName : resultArtists.keySet()) {
            objectNode2.put(artistName, resultArtists.get(artistName));
        }
        objectNode1.set("topArtists", objectNode2);

        objectNode2 = objectMapper.createObjectNode();

        for (Song song : songs.keySet()) {
            genres.put(song.getGenre(), genres
                    .getOrDefault(song.getGenre(), 0) + songs.get(song));
        }
        // merge songs with same name and same artist
        HashMap<Song, Integer> uniqueSongs = Wrapped.mergeDuplicateSongs(songs);
        LinkedHashMap<Song, Integer> resultSongs = Wrapped.createResults(
                uniqueSongs, SONG_COMPARATOR);
        for (Song song : resultSongs.keySet()) {
            objectNode2.put(song.getName(), resultSongs.get(song));
        }

        ObjectNode objectNode3 = objectMapper.createObjectNode();
        LinkedHashMap<String, Integer> genresResults = Wrapped.createResults(
                genres, NAME_COMPARATOR);
        for (String genre : genresResults.keySet()) {
            objectNode3.put(genre, genresResults.get(genre));
        }
        objectNode1.set("topGenres", objectNode3);
        objectNode1.set("topSongs", objectNode2);

        objectNode2 = objectMapper.createObjectNode();

        HashMap<Album, Integer> uniqueAlbums = Wrapped.mergeDuplicateAlbums(albums);
        LinkedHashMap<Album, Integer> albumsResults = Wrapped.createResults(
                uniqueAlbums, ALBUM_COMPARATOR);
        for (Album album : albumsResults.keySet()) {
            objectNode2.put(album.getName(), albumsResults.get(album));
        }
        objectNode1.set("topAlbums", objectNode2);

        objectNode2 = objectMapper.createObjectNode();
        LinkedHashMap<Episode, Integer> episodesResults = Wrapped.createResults(
                episodes, EPISODE_COMPARATOR);
        for (Episode episode : episodesResults.keySet()) {
            objectNode2.put(episode.getName(), episodesResults.get(episode));
        }
        objectNode1.set("topEpisodes", objectNode2);

        objectNode.set("result", objectNode1);
    }

    @Override
    public boolean isEmpty() {
        return songs.isEmpty()
                && artists.isEmpty()
                && albums.isEmpty()
                && episodes.isEmpty();
    }

    private void updateSong(final Song song, final Library library) {
        for (Artist artist : library.getArtists()) {
            for (Album album : artist.getAlbums()) {
                if (album.getSongs().contains(song)) {
                    album.incrementListened();
                    albums.put(album, albums.getOrDefault(album, 0) + 1);
                }
            }
        }
        user.getIncome().updateMonetizationSongs(song);
        songs.put(song, songs.getOrDefault(song, 0) + 1);
        artists.put(song.getArtist(), artists.getOrDefault(song.getArtist(), 0) + 1);
        Artist artist = (Artist) library.getUserOfType(song.getArtist(), MyConst.UserType.ARTIST);
        if (artist != null) {
            artist.getStats().addFan(user);
            user.getIncome().updateMonetizationArtists(artist);
        }
    }

    /**
     * Update the stats of a track played in the player.
     * This function also calls updateMonetization when needed.
     * Don't call other functions for updating monetization.
     * @param track the track loaded in the player
     * @param source the source loaded in the player
     */
    public void updateStats(final AudioFile track, final AudioObject source) {
        if (track == null) {
            return;
        }
        track.incrementListened();
        if (track.isAd()) {
            //for now, we only have Song ad
            Song ad = (Song) track;
            user.getIncome().updateMonetizationSongs(ad);
            return;
        }
        switch (track.getType()) {
            case SONG -> {
                Song song = (Song) track;
                updateSong(song, Admin.getLibrary());
            }
            case EPISODE -> {
                Episode episode = (Episode) track;
                episodes.put(episode, episodes.getOrDefault(episode, 0) + 1);

                Podcast podcast = (Podcast) source;
                Host host = (Host) Admin.getLibrary().getUserOfType(podcast.getOwner(),
                        MyConst.UserType.HOST);
                if (host != null) {
                    host.getStats().addEpisode(episode, user);
                }
            }
            default -> { //never
            }
        }
    }
}
