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
    @Getter
    private Double revenue = 0.0;
    //for ad
    @Getter
    private int price;

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

    @Override
    public String getOwner() {
        return getArtist();
    }

    @Override
    public boolean isAd() {
        return getGenre().equals("advertisement");
    }

    /**
     * Delete a song from library.
     * This function doesn't check if someone is playing the song.
     * @param library to access songs list and each user's playlist
     */
    public void delete(final Library library) {
        library.getSongs().remove(this);
        for (User user : library.getUsers()) {
            user.getLikedSongs().remove(this);
            for (Playlist playlist : user.getPlaylists()) {
                playlist.getSongs().remove(this);
            }
        }
    }

    public void addRevenue(Double money) {
        revenue += money;
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

    public void setPrice(int price) {
        this.price = price;
    }
}
