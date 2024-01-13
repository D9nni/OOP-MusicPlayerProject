package app.users.normal_stuff;

import java.util.ArrayList;

import app.audio.Song;
import app.audio.Library;
import app.audio.Podcast;
import app.audio.Playlist;
import app.audio.AudioObject;
import app.audio.Album;
import app.commands.Command;
import lombok.Getter;
import app.users.Artist;
import app.users.GeneralUser;
import app.users.Host;
import app.utils.MyConst;
import app.users.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SearchBar {

    @Getter
    private ArrayList<AudioObject> audioSearchResults = null;
    private ArrayList<GeneralUser> userSearchResults = null;
    private int numberOfResults = 0;
    private final User user;

    @Getter
    private AudioObject selectedAudio = null;
    @Getter
    private GeneralUser selectedUser = null;

    public SearchBar(final User user) {

        this.user = user;
    }

    /**
     * Select a result. Search must be run before.
     *
     * @param cmd        for timestamp
     * @param objectNode for output
     */
    public void select(final Command cmd, final ObjectNode objectNode) {
        if (!user.isConnected()) {
            user.standardOfflineCommand("select", objectNode);
            return;
        }
        selectedAudio = null;
        if (audioSearchResults == null && userSearchResults == null) {
            objectNode.put("message", "Please conduct a search before making a selection.");
        } else if (cmd.getItemNumber() > numberOfResults) {
            objectNode.put("message", "The selected ID is too high.");

        } else {
            if (userSearchResults != null) {
                int selectedId = cmd.getItemNumber() - 1;
                selectedUser = userSearchResults.get(selectedId);
                user.setCurrentPage(selectedUser.getCurrentPage()); // hack
                userSearchResults = null;
                audioSearchResults = null;
                objectNode.put("message", "Successfully selected "
                        + selectedUser.getUsername() + "'s page.");
            } else {
                user.setLastCommand(cmd.getCommand());
                int selectedId = cmd.getItemNumber() - 1;
                selectedAudio = audioSearchResults.get(selectedId);
                audioSearchResults = null;
                userSearchResults = null;
                objectNode.put("message", "Successfully selected "
                        + selectedAudio.getName() + ".");
            }
        }
    }

    /**
     * Search something in library.
     *
     * @param cmd        for timestamp and type of searched object
     * @param library    where to search from
     * @param objectNode for output
     */
    public void search(final Command cmd, final Library library, final ObjectNode objectNode) {

        if (!user.isConnected()) {
            user.standardOfflineCommand("search", objectNode);
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        userSearchResults = null;
        audioSearchResults = null;
        switch (cmd.getType()) {
            case "song":
                searchSong(cmd, library);
                break;
            case "podcast":
                searchPodcast(cmd, library);
                break;
            case "playlist":
                searchPlaylist(cmd, library);
                break;
            case "album":
                searchAlbum(cmd, library);
                break;
            case "artist":
                searchArtist(cmd, library);
                break;
            case "host":
                searchHost(cmd, library);
                break;
            default:
                //invalid search type
                return;
        }
        ArrayNode resultsArrayNode = objectMapper.createArrayNode();
        numberOfResults = 0;
        if (audioSearchResults != null) {
            numberOfResults = audioSearchResults.size();
            for (AudioObject result : audioSearchResults) {
                resultsArrayNode.add(result.getName());
            }
        } else if (userSearchResults != null) {
            for (GeneralUser result : userSearchResults) {
                resultsArrayNode.add(result.getUsername());
            }
            numberOfResults = userSearchResults.size();
        }
        objectNode.put("message", "Search returned " + numberOfResults + " results");
        objectNode.set("results", resultsArrayNode);
    }

    private void searchSong(final Command cmd, final Library library) {
        audioSearchResults = new ArrayList<>();
        for (Song song : library.getSongs()) {
            if (cmd.getFilters().songFitsSearch(song)) {
                audioSearchResults.add(song);
                if (audioSearchResults.size() == MyConst.RESULT_SIZE) {
                    return;
                }
            }
        }
    }

    private void searchPodcast(final Command cmd, final Library library) {
        audioSearchResults = new ArrayList<>();
        for (Podcast podcast : library.getPodcasts()) {
            if (cmd.getFilters().podcastFitsSearch(podcast)) {
                audioSearchResults.add(podcast);
                if (audioSearchResults.size() == MyConst.RESULT_SIZE) {
                    return;
                }
            }
        }
    }

    private void searchPlaylist(final Command cmd, final Library library) {
        audioSearchResults = new ArrayList<>();
        for (User user1 : library.getUsers()) {
            for (Playlist playlist : user1.getPlaylists()) {
                if (cmd.getFilters().playlistFitsSearch(playlist)) {
                    if (playlist.isVisible() || playlist.getOwner().equals(cmd.getUsername())) {
                        audioSearchResults.add(playlist);
                        if (audioSearchResults.size() == MyConst.RESULT_SIZE) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private void searchAlbum(final Command cmd, final Library library) {
        audioSearchResults = new ArrayList<>();
        for (Artist artist : library.getArtists()) {
            for (Album album : artist.getAlbums()) {
                if (cmd.getFilters().albumFitsSearch(album)) {
                    audioSearchResults.add(album);
                    if (audioSearchResults.size() == MyConst.RESULT_SIZE) {
                        return;
                    }
                }
            }
        }
    }

    private void searchArtist(final Command cmd, final Library library) {
        userSearchResults = new ArrayList<>();
        for (Artist artist : library.getArtists()) {
            if (cmd.getFilters().artistFitsSearch(artist)) {
                userSearchResults.add(artist);
                if (userSearchResults.size() == MyConst.RESULT_SIZE) {
                    return;
                }
            }
        }
    }

    private void searchHost(final Command cmd, final Library library) {
        userSearchResults = new ArrayList<>();
        for (Host host : library.getHosts()) {
            if (cmd.getFilters().hostFitsSearch(host)) {
                userSearchResults.add(host);
                if (userSearchResults.size() == MyConst.RESULT_SIZE) {
                    return;
                }
            }
        }
    }

}
