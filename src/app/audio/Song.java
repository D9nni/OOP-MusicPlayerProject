package app.audio;

import fileio.input.SongInput;
import lombok.Getter;
import app.users.User;
import app.utils.MyConst;

import java.util.ArrayList;

public final class Song extends AudioFile {
    private final SongInput songInput;
    @Getter
    private int likes = 0;

    /**
     *
     * @return string in format "name - artist"
     */
    @Override
    public String toString() {
        return getName() + " - " + getArtist();
    }

    public Song(final SongInput songInput) {
        this.songInput = songInput;

    }

    public Song(final Song song) {
        songInput = new SongInput();
        this.songInput.setName(song.getName());
        this.songInput.setAlbum(song.getAlbum());
        this.songInput.setArtist(song.getArtist());
        this.songInput.setDuration(song.getDuration());
        this.songInput.setGenre(song.getGenre());
        this.songInput.setLyrics(song.getLyrics());
        this.songInput.setTags(song.getTags());
        this.songInput.setReleaseYear(song.getReleaseYear());
        likes = 0;
    }


    /**
     * increment number of likes
     */
    public void like() {
        likes++;
    }

    /**
     * decrement number of likes
     */
    public void dislike() {
        likes--;
    }

    /**
     *
     * @return SONG type
     */
    @Override
    public MyConst.SourceType getType() {
        return MyConst.SourceType.SONG;
    }

    /**
     * Delete a song from library.
     * This function doesn't check if someone is playing the song.
     * @param library to access songs list and each user's playlist
     */
    public void delete(final Library library) {
        library.getSongs().removeIf(obj -> obj.getName().equals(this.getName()));
        for (User user : library.getUsers()) {
            user.getLikedSongs().removeIf(obj -> obj.getName().equals(this.getName()));
            for (Playlist playlist : user.getPlaylists()) {
                playlist.getSongs().removeIf(obj -> obj.getName().equals(this.getName()));
            }
        }
    }


    public String getName() {
        return songInput.getName();
    }
    /**
     *
     * @return duration
     */

    @Override
    public Integer getDuration() {
        return songInput.getDuration();
    }


    public String getAlbum() {
        return songInput.getAlbum();
    }


    public ArrayList<String> getTags() {
        return songInput.getTags();
    }


    public String getLyrics() {
        return songInput.getLyrics();
    }


    public String getGenre() {
        return songInput.getGenre();
    }


    public int getReleaseYear() {
        return songInput.getReleaseYear();
    }


    public String getArtist() {
        return songInput.getArtist();
    }

}
