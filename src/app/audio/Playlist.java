package app.audio;

import lombok.Getter;
import app.users.User;
import app.utils.MyConst;

import java.util.ArrayList;

@Getter
public final class Playlist extends AudioCollection {
    private final String name;
    private final User owner;
    private final int creationTime;
    private boolean visible;
    private final ArrayList<Song> songs;
    private int followers = 0;

    public Playlist(final String name, final ArrayList<Song> songs, final User owner,
                    final int creationTime, final boolean visible) {
        this.name = name;
        this.owner = owner;
        this.songs = songs;
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
    @Override
    public String getOwner() {
        return owner.getUsername();
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
     * Increment the number of followers and send a notification to owner.
     * @param username the username who started following the playlist
     */
    public void follow(final String username) {
        String message = "Follow Playlist: " + username + " started following "
                + name + " playlist.";
        owner.receiveNotification(message);
        followers++;
    }

    /**
     * Decrement the number of followers and send a notification to owner.
     * @param username the username who unfollowed the playlist
     */
    public void unfollow(final String username) {
        String message = "Unfollow Playlist: " + username + " unfollowed " + name + " playlist.";
        owner.receiveNotification(message);
        followers--;
    }

    /**
     *
     * @param vis set visibility to given value
     */
    public void setVisibility(final boolean vis) {
        this.visible = vis;
    }

}
