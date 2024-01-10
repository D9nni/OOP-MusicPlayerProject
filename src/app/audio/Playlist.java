package app.audio;

import lombok.Getter;
import app.users.User;
import app.utils.MyConst;

import java.util.ArrayList;

@Getter
public class Playlist extends AudioCollection {
    private String name;
    private String owner;
    private int creationTime;
    private boolean visible = true;
    private final ArrayList<Song> songs = new ArrayList<>();
    private int followers = 0;

    public Playlist(String name, ArrayList<Song> songs, String owner, int creationTime, boolean visible) {
        this.name = name;
        this.owner = owner;
        this.creationTime = creationTime;
        this.visible = visible;
    }

    /**
     * Calculate total likes.
     * @return sum of all songs likes
     */
    public int calculateLikes() {
        int totalLikes = 0;
        for (Song song : songs) {
            totalLikes += song.getLikes();
        }
        return totalLikes;
    }

    /**
     *
     * @return a string in format "name - owner"
     */
    @Override
    public String toString() {
        return name + " - " + owner;
    }


    /**
     *
     * @return total duration of songs
     */
    @Override
    public Integer getDuration() {
        Integer time = 0;
        for (Song song : songs) {
            time += song.getDuration();
        }
        return time;
    }

    /**
     *
     * @return songs upcasted to AudioFile
     */
    @Override
    public ArrayList<AudioFile> getTracks() {
        return new ArrayList<>(songs);
    }

    /**
     *
     * @return PLAYLIST type
     */
    public MyConst.SourceType getType() {
        return MyConst.SourceType.PLAYLIST;
    }

    /**
     *
     * @param owner set owner name
     */
    public void setOwner(final String owner) {
        this.owner = owner;
    }

    /**
     *
     * @param creationTime set timestamp when it was created
     */
    public void setCreationTime(final int creationTime) {
        this.creationTime = creationTime;
    }

    /**
     *
     * @param song will be added to songs list
     */
    public void addSong(final Song song) {
            songs.add(song);
    }

    /**
     *
     * @param song will be removed from songs list
     */
    public void removeSong(final Song song) {
        songs.remove(song);
    }

    /**
     *
     * @param song check if song exists in list
     * @return true if playlist contains song
     */
    public boolean containsSong(final Song song) {
        return songs.contains(song);
    }

    /**
     *
     * @param songName name of searched song
     * @return true if playlist contains a song with name equal to songName
     */
    public boolean containsSong(final String songName) {
        return songs.stream().anyMatch(obj -> obj.getName().equals(songName));
    }

    /**
     * Delete the Playlist from library.
     * Removes the playlist from followedPlaylists list of each user.
     * @param library for getting the followedPlaylists lists
     */
    public void delete(final Library library) {
        for (User user : library.getUsers()) {
            user.getFollowedPlaylists().remove(this);
        }
    }

    /**
     * increment number of followers
     */
    public void follow() {
        followers++;
    }

    /**
     * decrement number of followers
     */
    public void unfollow() {
        followers--;
    }

    /**
     *
     * @param vis set visibility to given value
     */
    public void setVisibility(final boolean vis) {
        this.visible = vis;
    }

    /**
     * @param name name of playlist
     */
    public void setName(final String name) {
        this.name = name;
    }
}
