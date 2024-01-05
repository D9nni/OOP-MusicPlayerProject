package fileio.input;

import app.audio.Album;
import app.audio.Podcast;
import app.audio.Song;

import java.util.ArrayList;

import app.audio.Playlist;
import lombok.Getter;
import app.users.Artist;
import app.users.Host;

@Getter
public final class Filters {
    private String name;
    private String album;
    private ArrayList<String> tags;
    private String lyrics;
    private String genre;
    private String releaseYear;
    private String artist;
    private String owner;
    private String description;
    private String username;

    /**
     * Check if a song fits search filters.
     * @param song the song to be checked
     * @return true if it fits
     */
    public boolean songFitsSearch(final Song song) {
        boolean retval = true;
        //verify each field
        if (name != null && !song.getName().toLowerCase().startsWith(name.toLowerCase())) {
            retval = false;
        }
        if (album != null && !album.equals(song.getAlbum())) {
            retval = false;
        }
        if (lyrics != null && !song.getLyrics().toLowerCase().
                contains(lyrics.toLowerCase())) {
            retval = false;
        }
        if (genre != null && !song.getGenre().equalsIgnoreCase(genre)) {
            retval = false;
        }
        if (releaseYear != null) {
            String sign = releaseYear.substring(0, 1);
            if (sign.equals("<") || sign.equals(">")) {
                String newReleaseYear = releaseYear.substring(1);
                int intReleaseYear = Integer.parseInt(newReleaseYear);
                switch (sign) {
                    case ">":
                        if (!(song.getReleaseYear() > intReleaseYear)) {
                            retval = false;
                        }
                        break;
                    case "<":
                        if (!(song.getReleaseYear() < intReleaseYear)) {
                            retval = false;
                        }
                        break;
                    default:
                        if (!(song.getReleaseYear() == intReleaseYear)) {
                            retval = false;
                        }
                        break;
                }
            }

        }

        if (artist != null && !artist.equals(song.getArtist())) {
            retval = false;
        }
        //verify each tag from list
        if (tags != null) {
            for (String filterTag : tags) {
                if (!song.getTags().contains(filterTag)) {
                    retval = false;
                    break;
                }
            }
        }
        return retval;

    }

    /**
     * Check if a playlist fits search filters.
     * @param playlist playlist to be checked
     * @return true if fits
     */
    public boolean playlistFitsSearch(final Playlist playlist) {
        boolean retval = true;
        if (name != null && !(playlist.getName().startsWith(name))) {
            retval = false;
        }
        if (owner != null && !this.owner.equals(playlist.getOwner())) {
            retval = false;
        }
        return retval;
    }

    /**
     * check if podcast fits search filters
     * @param podcast podcast to be checked
     * @return true if fits
     */
    public boolean podcastFitsSearch(final Podcast podcast) {
        boolean retval = true;
        if (name != null && !(podcast.getName().startsWith(name))) {
            retval = false;
        }
        if (owner != null && !(owner.equals(podcast.getOwner()))) {
            retval = false;
        }
        return retval;
    }

    /**
     * Check if an album fits search filters.
     * @param album1 the album to be checked
     * @return true if fits
     */
    public boolean albumFitsSearch(final Album album1) {
        boolean retval = true;
        if (name != null && !album1.getName().startsWith(name)) {
            retval = false;
        }
        if (owner != null && !album1.getArtist().startsWith(owner)) {
            retval = false;
        }
        if (description != null && !album1.getDescription().startsWith(description)) {
            retval = false;
        }
        return retval;
    }

    /**
     * Check if an artist fits search filters.
     * @param artist1 the artist
     * @return true if fits
     */
    public boolean artistFitsSearch(final Artist artist1) {
        return name == null || artist1.getUsername().startsWith(name);
    }

    /**
     * Check if a host fits search filters.
     * @param host the host
     * @return true if fits
     */
    public boolean hostFitsSearch(final Host host) {
        return name == null || host.getUsername().startsWith(name);
    }

    public void setOwner(final String owner) {
        this.owner = owner;
    }


    public void setName(final String name) {
        this.name = name;
    }

    public void setAlbum(final String album) {
        this.album = album;
    }

    public void setTags(final ArrayList<String> tags) {
        this.tags = tags;
    }

    public void setLyrics(final String lyrics) {
        this.lyrics = lyrics;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setGenre(final String genre) {
        this.genre = genre;
    }

    public void setReleaseYear(final String releaseYear) {
        this.releaseYear = releaseYear;
    }

    public void setArtist(final String artist) {
        this.artist = artist;
    }

    public void setUsername(final String username) {
        this.username = username;
    }
}
