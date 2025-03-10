package app.audio;

import fileio.input.SongInput;
import lombok.Getter;
import app.utils.MyConst;

import java.util.ArrayList;
@Getter
public final class Album extends AudioCollection {
    private final String name;
    private final String artist;
    private final int releaseYear;
    private final String description;
    private final ArrayList<Song> songs;

    public Album(final String name, final String artist, final int releaseYear,
                 final String description, final ArrayList<SongInput> songsInput,
                 final Library library) {
        this.name = name;
        this.artist = artist;
        this.releaseYear = releaseYear;
        this.description = description;
        songs = new ArrayList<>();
        for (SongInput songInput : songsInput) {
            Song song = new Song(songInput);
            songs.add(song);
        }
        library.getSongs().addAll(songs);
    }


    /**
     * Calculate total number of likes
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
     * @return album name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     *
     * @return duration as sum of songs duration
     */
    @Override
    public Integer getDuration() {
        int duration = 0;
        for (Song song : songs) {
            duration += song.getDuration();
        }
        return duration;
    }

    /**
     *
     * @return ALBUM type
     */
    @Override
    public MyConst.SourceType getType() {
        return MyConst.SourceType.ALBUM;
    }

    /**
     *
     * @return album owner (artist's name)
     */
    @Override
    public String getOwner() {
        return getArtist();
    }

    /**
     *
     * @return the list of songs upcasted to AudioFile
     */
    @Override
    public ArrayList<AudioFile> getTracks() {
        return new ArrayList<>(songs);
    }
}
