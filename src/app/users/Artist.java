package app.users;

import app.analytics.monetization.ArtistIncome;
import app.analytics.wrapped.ArtistStats;
import app.audio.Album;
import app.audio.Song;
import app.audio.Playlist;
import app.audio.AudioFile;
import app.audio.Library;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.SongInput;
import app.commands.Command;
import lombok.Getter;
import app.pages.ArtistPage;
import app.users.artist_stuff.ArtistEvent;
import app.users.artist_stuff.Merch;
import app.utils.MyConst;
import app.utils.ValidateInput;

import java.util.ArrayList;

@Getter
public class Artist extends GeneralUser {
    private final ArrayList<Album> albums = new ArrayList<>();
    private final ArtistPage artistPage = new ArtistPage(this);
    private final ArrayList<ArtistEvent> artistEvents = new ArrayList<>();
    private final ArrayList<Merch> merch = new ArrayList<>();
    private final ArtistStats stats = new ArtistStats(this);
    private final ArtistIncome income = new ArtistIncome(this);

    /**
     * Calculate total likes.
     * @return sum of all songs likes from all albums
     */
    public int calculateLikes() {
        int totalLikes = 0;
        for (Album album : albums) {
            totalLikes += album.calculateLikes();
        }
        return totalLikes;
    }

    /**
     * Add a new album.
     * Adds the songs from the new album in library.
     * @param library for adding the songs
     * @param cmd for data
     * @param objectNode for output
     */
    public void addAlbum(final Library library, final Command cmd, final ObjectNode objectNode) {
        if (albums.stream().anyMatch(obj ->
                obj.getName().equals(cmd.getName()) && obj.getArtist().equals(cmd.getUsername()))) {
            objectNode.put("message", cmd.getUsername()
                    + " has another album with the same name.");
        } else {
            ArrayList<SongInput> songInputs = cmd.getSongs();
            boolean sameSongTwice = false;
            for (SongInput songInput : songInputs) {
                int numberEquals = 0;
                for (SongInput songInput1 : songInputs) {
                    if (songInput.getName().equals(songInput1.getName())) {
                        numberEquals++;
                    }
                }
                if (numberEquals >= 2) {
                    sameSongTwice = true;
                    break;
                }
            }
            if (sameSongTwice) {
                objectNode.put("message", cmd.getUsername()
                        + " has the same song at least twice in this album.");
            } else {
                Album newAlbum = new Album(cmd.getName(), cmd.getUsername(),
                        cmd.getReleaseYear(), cmd.getDescription(), cmd.getSongs(),
                        library);
                albums.add(newAlbum);
                objectNode.put("message", cmd.getUsername()
                        + " has added new album successfully.");
            }
        }

    }

    /**
     * Remove an album.
     * Removes the songs from library.
     * @param library for removing the songs.
     * @param cmd for data
     * @param objectNode for output
     */
    public void removeAlbum(final Library library, final Command cmd, final ObjectNode objectNode) {
        String name = cmd.getName();
        Album removedAlbum = null;
        for (Album album : albums) {
            if (album.getName().equals(name)) {
                removedAlbum = album;
                break;
            }
        }
        if (removedAlbum == null) {
            objectNode.put("message", getUsername()
                    + " doesn't have an album with the given name.");
        } else {
            boolean canBeRemoved = true;
            for (User user : library.getUsers()) {
                if (user.getPlayer().isPlaying(cmd.getTimestamp())) {
                    switch (user.getPlayer().getSourceType()) {
                        case ALBUM:
                            if (user.getPlayer().getSource().equals(removedAlbum)) {
                                canBeRemoved = false;
                                break;
                            }
                            break;
                        case PLAYLIST:
                            Playlist playlist = (Playlist) user.getPlayer().getSource();
                            for (Song song : removedAlbum.getSongs()) {
                                if (playlist.containsSong(song.getName())) {
                                    canBeRemoved = false;
                                    break;
                                }
                            }
                            break;
                        case SONG:
                            Song song = (Song) user.getPlayer().getTrack();
                            if (removedAlbum.containsSongByNameAndArtist(song)) {
                                canBeRemoved = false;
                                break;
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            if (!canBeRemoved) {
                objectNode.put("message", getUsername() + " can't delete this album.");
            } else {
                albums.remove(removedAlbum);
                stats.getRemovedAlbums().add(removedAlbum);
                for (Song song : removedAlbum.getSongs()) {
                    song.delete(library);
                }
                objectNode.put("message", getUsername()
                        + " deleted the album successfully.");
            }
        }
    }

    /**
     * Add a new event.
     * @param cmd for data
     * @param objectNode for output
     */
    public void addEvent(final Command cmd, final ObjectNode objectNode) {
        String date = cmd.getDate();
        if (!ValidateInput.isValidDateFormat(date)) {
            objectNode.put("message", "Event for " + super.getUsername()
                    + " does not have a valid date.");
        } else {
            boolean sameEvent = false;
            for (ArtistEvent artistEvent : artistEvents) {
                if (artistEvent.name().equals(cmd.getName())) {
                    sameEvent = true;
                    break;
                }
            }
            if (sameEvent) {
                objectNode.put("message", super.getUsername()
                        + " has another event with the same name.");
            } else {
                ArtistEvent artistEvent = new ArtistEvent(cmd.getName(),
                        cmd.getDescription(), cmd.getDate());
                artistEvents.add(artistEvent);
                objectNode.put("message", super.getUsername()
                        + " has added new event successfully.");
            }
        }
    }

    /**
     * Remove an event.
     * @param cmd for data
     * @param objectNode for output
     */
    public void removeEvent(final Command cmd, final ObjectNode objectNode) {
        if (artistEvents.removeIf(obj -> obj.name().equals(cmd.getName()))) {
            objectNode.put("message", getUsername()
                    + " deleted the event successfully.");
        } else {
            objectNode.put("message", getUsername()
                    + " doesn't have an event with the given name.");
        }
    }

    /**
     * Add a new merch.
     * @param cmd for data
     * @param objectNode for output
     */
    public void addMerch(final Command cmd, final ObjectNode objectNode) {
        if (cmd.getPrice() < 0) {
            objectNode.put("message", "Price for merchandise can not be negative.");
        } else {
            boolean sameMerch = false;
            for (Merch merch1 : merch) {
                if (merch1.name().equals(cmd.getName())) {
                    sameMerch = true;
                    break;
                }
            }
            if (sameMerch) {
                objectNode.put("message", super.getUsername()
                        + " has merchandise with the same name.");
            } else {
                Merch merch1 = new Merch(cmd.getName(), cmd.getDescription(), cmd.getPrice());
                merch.add(merch1);
                objectNode.put("message", super.getUsername()
                        + " has added new merchandise successfully.");
            }
        }
    }

    /**
     * Show albums.
     * Show a list of contained songs names for each album.
     * @param objectNode for output
     */
    public void showAlbums(final ObjectNode objectNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode mainNode = objectMapper.createArrayNode();


        for (Album album : albums) {
            ObjectNode oneResult = objectMapper.createObjectNode();
            oneResult.put("name", album.getName());
            ArrayNode resultSongs = objectMapper.createArrayNode();
            for (AudioFile track : album.getTracks()) {
                resultSongs.add(track.getName());
            }
            oneResult.set("songs", resultSongs);
            mainNode.add(oneResult);

        }
        objectNode.set("result", mainNode);
    }

    /**
     * Check if artist has an album.
     * @param source the album checked
     * @return true if he has
     */
    public boolean hasAlbum(final Album source) {
        return albums.stream().anyMatch(obj -> obj.equals(source));
    }

    /**
     * Check if artist has a song with this name in his albums.
     * @param song a song
     * @return true if he has
     */
    public boolean hasSong(final Song song) {
        for (Album album : albums) {
            if (album.containsSongByNameAndArtist(song)) {
                return true;
            }
        }
        return false;
    }

    public Artist(final String username, final String city, final int age) {
        super(username, city, age, MyConst.UserType.ARTIST);
        super.setCurrentPage(artistPage);
    }

    /**
     * Delete an artist.
     * Will succeed only if no one is using his stuff now.
     * @param library for updating library
     * @param timestamp the current time
     * @return true if it was deleted
     */
    @Override
    public boolean delete(final Library library, final int timestamp) {
        boolean canBeDeleted = true;
        for (User user : library.getUsers()) {
            //check the current page of each user
            if (user.getCurrentPage().equals(this.getCurrentPage())) {
                canBeDeleted = false;
                break;
            }
            //check the player of each user
            if (user.getPlayer().isPlaying(timestamp)) {
                MyConst.SourceType sourceType = user.getPlayer().getSourceType();
                // check if someone is playing one of the artist's albums
                if (sourceType == MyConst.SourceType.ALBUM) {
                    Album source = (Album) user.getPlayer().getSource();
                    if (this.hasAlbum(source)) {
                        canBeDeleted = false;
                        break;
                    }
                } else {
                    if (user.getPlayer().getTrack().getType() == MyConst.SourceType.SONG) {
                        Song track = (Song) user.getPlayer().getTrack();
                        //check if someone is playing one of artist's songs
                        if (this.hasSong(track)) {
                            canBeDeleted = false;
                            break;
                        }
                    }
                }
            }
        }
        if (canBeDeleted) {
            // delete all songs
            for (Album album : this.getAlbums()) {
                for (Song song : album.getSongs()) {
                    song.delete(library);
                }
            }
            library.getArtists().remove(this);
        }
        return canBeDeleted;
    }

}
