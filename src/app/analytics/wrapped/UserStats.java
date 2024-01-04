package app.analytics.wrapped;

import app.audio.*;
import app.users.Artist;
import app.users.User;
import app.utils.MyConst;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

public class UserStats implements Wrapped {
    private final HashMap<Song, Integer> songs = new HashMap<>();
    private final HashMap<String, Integer> artists = new HashMap<>(); //artist may not exist as instance
    private final HashMap<Album, Integer> albums = new HashMap<>();
    private final HashMap<Episode, Integer> episodes = new HashMap<>();
    private final HashMap<String, Integer> genres = new HashMap<>();
    private final User user;
    private static Library library;

    public static void setLibrary(Library library1) {
        library = library1;
    }


    public UserStats(User user) {
        this.user = user;
    }

    @Override
    public void wrapped(ObjectNode objectNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode1 = objectMapper.createObjectNode();
        ObjectNode objectNode2 = objectMapper.createObjectNode();
        LinkedHashMap<String, Integer> resultArtists = Wrapped.createResults(artists, artistComparator);
        for (String artistName : resultArtists.keySet()) {
            objectNode2.put(artistName, resultArtists.get(artistName));
        }
        objectNode1.set("topArtists", objectNode2);

        objectNode2 = objectMapper.createObjectNode();

        for (Song song : songs.keySet()) {
            genres.put(song.getGenre(), genres.getOrDefault(song.getGenre(), 0) + songs.get(song));
        }
        // merge songs with same name and same artist
        HashMap <Song, Integer> uniqueSongs = removeDuplicates(songs);
        LinkedHashMap<Song, Integer> resultSongs = Wrapped.createResults(uniqueSongs, songComparator);
        for (Song song : resultSongs.keySet()) {
            objectNode2.put(song.getName(), resultSongs.get(song));
        }

        ObjectNode objectNode3 = objectMapper.createObjectNode();
        LinkedHashMap<String, Integer> genresResults = Wrapped.createResults(genres, genreComparator);
        for (String genre : genresResults.keySet()) {
            objectNode3.put(genre, genresResults.get(genre));
        }
        objectNode1.set("topGenres", objectNode3);
        objectNode1.set("topSongs", objectNode2);

        objectNode2 = objectMapper.createObjectNode();
        LinkedHashMap<Album, Integer> albumsResults = Wrapped.createResults(albums, albumComparator);
        for (Album album : albumsResults.keySet()) {
            objectNode2.put(album.getName(), albumsResults.get(album));
        }
        objectNode1.set("topAlbums", objectNode2);

        objectNode2 = objectMapper.createObjectNode();
        LinkedHashMap<Episode, Integer> episodesResults = Wrapped.createResults(episodes, episodeComparator);
        for (Episode episode : episodesResults.keySet()) {
            objectNode2.put(episode.getName(), episodesResults.get(episode));
        }
        objectNode1.set("topEpisodes", objectNode2);

        objectNode.set("result", objectNode1);
    }

    public void updateStats(AudioFile track, AudioObject source) {
        if (track != null) {
            track.incrementListened();
            switch (track.getType()) {
                case SONG -> {
                    if (source.getType() == MyConst.SourceType.ALBUM) {
                        Album album = (Album) source;
                        albums.put(album, albums.getOrDefault(album, 0) + 1);
                        album.incrementListened();
                    } else {
                        Song song = (Song) track;
                        for (Artist artist : library.getArtists()) {
                            for (Album album : artist.getAlbums()) {
                                if (album.containsSong(song)) {
                                    albums.put(album, albums.getOrDefault(album, 0) + 1);
                                    album.incrementListened();
                                }
                            }
                        }

                    }
                    Song song = (Song) track;
                    songs.put(song, songs.getOrDefault(song, 0) + 1);
                    artists.put(song.getArtist(), artists.getOrDefault(song.getArtist(), 0) + 1);
                    Artist artist = (Artist) library.getUserOfType(song.getArtist(), MyConst.UserType.ARTIST);
                    if (artist != null) {
                        artist.getStats().addFan(user);
                    }
                }
                case EPISODE -> {
                    Episode episode = (Episode) track;
                    episodes.put(episode, episodes.getOrDefault(episode, 0) + 1);
                }
            }
        }
    }

    public void updateSource(AudioObject source) {
        switch (source.getType()) {
            case ALBUM -> {
                Album album = (Album) source;
                albums.put(album, albums.getOrDefault(album, 0) + 1);
            }
            case SONG -> {
                Song song = (Song) source;
                songs.put(song, songs.getOrDefault(song, 0) + 1);
                artists.put(song.getArtist(), artists.getOrDefault(song.getArtist(), 0) + 1);
                for (Artist artist : library.getArtists()) {
                    for (Album album : artist.getAlbums()) {
                        if (album.containsSong(song)) {
                            albums.put(album, albums.getOrDefault(album, 0) + 1);
                        }
                    }
                }
            }
        }
    }
    private HashMap<Song, Integer> removeDuplicates(HashMap<Song, Integer> songs) {
        HashMap <Song, Integer> result = new HashMap<>();
        for (Song song : songs.keySet()) {
            Song song1 = containsSong(result, song);
            if(song1 != null) {
                result.put(song1, result.get(song1) + songs.get(song));
            } else {
                result.put(song, songs.get(song));
            }
        }
        return songs;
    }
    private Song containsSong(HashMap<Song, Integer> songs, Song song) {
        for (Song song1 : songs.keySet()) {
            if (song1.getName().equals(song.getName()) && song1.getArtist().equals(song.getArtist())) {
                return song1;
            }
        }
        return null;
    }

}
