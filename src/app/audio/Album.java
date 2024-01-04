package app.audio;

import fileio.input.SongInput;
import lombok.Getter;
import app.utils.MyConst;

import java.util.ArrayList;
@Getter
public class Album extends AudioCollection {
    private final String name;
    private final String artist;
    private final int releaseYear;
    private final String description;
    private final ArrayList<Song> songs;

    public Album(final String name, final String artist, final int releaseYear,
                 final String description, final ArrayList<SongInput> songsInput) {
        this.name = name;
        this.artist = artist;
        this.releaseYear = releaseYear;
        this.description = description;
        songs = new ArrayList<>();
        for (SongInput songInput : songsInput) {
            Song song = new Song(songInput);
            //TODO: MODIFICA INCAT SA AI AICI ACEEASI INSTANTA DIN LIBRARY CA SONG
            songs.add(song);
        }
    }

    /**
     * Check if album contains a song
     * @param song a song
     * @return true if album has a song with name equal to given name
     */
    public boolean containsSong(final Song song) {
        String songName1 = song.getName();
        String artist1 = song.getArtist();
        return songs.stream().anyMatch(obj -> obj.getName().equals(songName1) && obj.getArtist().equals(artist1));
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
     * @return the list of songs upcasted to AudioFile
     */
    @Override
    public ArrayList<AudioFile> getTracks() {
        return new ArrayList<>(songs);
    }

}
