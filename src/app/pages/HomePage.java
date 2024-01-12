package app.pages;


import app.audio.AudioObject;
import app.audio.Playlist;
import app.audio.Song;
import app.users.Admin;
import app.users.Artist;
import com.fasterxml.jackson.databind.node.ObjectNode;
import app.users.User;
import app.utils.MyConst;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

public final class HomePage implements Page {
    @Getter
    private final User owner;
    private final ArrayList<Song> songRecommendations = new ArrayList<>();
    private final ArrayList<Playlist> playlistsRecommendations = new ArrayList<>();
    @Getter
    private AudioObject lastRecommendation;
    public static final Integer[] SONGS_BY_GENRE = new Integer[]{5, 3, 2};
    public static final Integer NR_TOP_GENRES = 3;
    private static final Integer MINIMUM_TRACK_SEEK_FOR_SEED = 30;

    public HomePage(User owner) {

        this.owner = owner;
    }

    @Override
    public void printPage(final ObjectNode objectNode) {
        User user = owner;
        //Liked songs:\n\t[Melodic Mirage]\n\nFollowed playlists:\n\t[]
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Liked songs:\n\t[");

        ArrayList<Song> likedSongs = new ArrayList<>(user.getLikedSongs());
        likedSongs.sort((a, b) -> Integer.compare(b.getLikes(), a.getLikes()));
        Page.appendAudioListToStringBuilder(likedSongs, stringBuilder);

        stringBuilder.append("]\n\nFollowed playlists:\n\t[");
        ArrayList<Playlist> followedPlaylists = new ArrayList<>(user.getFollowedPlaylists());
        followedPlaylists.sort((a, b) -> Integer.compare(b.calculateLikes(), a.calculateLikes()));
        Page.appendAudioListToStringBuilder(followedPlaylists, stringBuilder);

        stringBuilder.append("]\n\nSong recommendations:\n\t[");
        Page.appendAudioListToStringBuilder(songRecommendations, stringBuilder);

        stringBuilder.append("]\n\nPlaylists recommendations:\n\t[");
        Page.appendAudioListToStringBuilder(playlistsRecommendations, stringBuilder);
        stringBuilder.append("]");
        objectNode.put("message", stringBuilder.toString());

    }

    public boolean randomSongRec(int trackSeek, Song track) {
        if (trackSeek < MINIMUM_TRACK_SEEK_FOR_SEED) {
            return false;
        }
        String genre = track.getGenre();
        ArrayList<Song> genreSongs = new ArrayList<>();
        for (Song song : Admin.getLibrary().getSongs()) {
            if (song.getGenre().equals(genre)) {
                genreSongs.add(song);
            }
        }
        if (genreSongs.isEmpty()) {
            return false;
        }
        Random random = new Random(trackSeek);
        int searchedIndex = random.nextInt(0, genreSongs.size());
        Song randomSong = genreSongs.get(searchedIndex);
        songRecommendations.add(randomSong);
        lastRecommendation = randomSong;
        return true;
    }

    public boolean randomPlaylistRec(int timestamp) {
        ArrayList<Song> allSongs = new ArrayList<>(owner.getLikedSongs());
        ArrayList<Playlist> allPlaylists = new ArrayList<>(owner.getPlaylists());
        allPlaylists.addAll(owner.getFollowedPlaylists());
        HashMap<String, ArrayList<Song>> genres = new HashMap<>();
        for (Playlist playlist : allPlaylists) {
            allSongs.addAll(playlist.getSongs());
        }
        if (allSongs.isEmpty()) {
            return false;
        }
        for (Song song : allSongs) {
            ArrayList<Song> genreSongs = genres.get(song.getGenre());
            if (genreSongs == null) {
                genreSongs = new ArrayList<>();
            }
            genreSongs.add(song);
            genres.put(song.getGenre(), genreSongs);
        }
        Comparator<Map.Entry<String, ArrayList<Song>>> genreComparator = Comparator
                .<Map.Entry<String, ArrayList<Song>>, Integer>comparing(entry -> entry.getValue().size(),
                        Comparator.reverseOrder())
                .thenComparing(Map.Entry::getKey);
        LinkedHashMap<String, ArrayList<Song>> sortedGenres = genres.entrySet()
                .stream()
                .sorted(genreComparator)
                .limit(NR_TOP_GENRES)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        ArrayList<Song> playlistSongs = new ArrayList<>();
        int i = 0;
        for (String genre : sortedGenres.keySet()) {
            ArrayList<Song> songs = sortedGenres.get(genre);
            List<Song> songsForGenre = songs.stream()
                    .sorted((obj1, obj2) -> obj2.getLikes() - obj1.getLikes())
                    .limit(SONGS_BY_GENRE[i])
                    .toList();
            playlistSongs.addAll(songsForGenre);
            i++;
        }
        String playlistName = owner.getUsername() + "'s recommendations";
        Playlist randomPlaylist = new Playlist(playlistName, playlistSongs, owner, timestamp, true);
        playlistsRecommendations.add(randomPlaylist);
        lastRecommendation = randomPlaylist;
        return true;

    }

    public boolean fansPlaylistsRec(String artistName, int timestamp) {
        Artist artist = (Artist) Admin.getLibrary().getUserOfType(artistName, MyConst.UserType.ARTIST);
        assert artist != null;
        ArrayList<User> fans = artist.getStats().getTop5Fans();
        ArrayList<Song> allSongs = new ArrayList<>();
        for (User fan : fans) {
            allSongs.addAll(fan.getTop5LikedSongs());
        }
        if (allSongs.isEmpty()) {
            return false;
        }
        allSongs.sort((obj1, obj2) -> obj2.getLikes() - obj1.getLikes());
        String playlistName = artistName + " Fan Club recommendations";
        Playlist fansPlaylist = new Playlist(playlistName, allSongs, owner, timestamp, true);
        playlistsRecommendations.add(fansPlaylist);
        lastRecommendation = fansPlaylist;
        return true;
    }
}
