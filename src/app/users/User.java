package app.users;

import app.analytics.monetization.UserIncome;
import app.analytics.wrapped.UserStats;
import app.analytics.wrapped.Wrapped;
import app.audio.*;
import app.commands.Command;
import app.observer.Observable;
import app.pages.Page;
import app.users.normal_stuff.Player;
import app.users.normal_stuff.SearchBar;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import app.pages.HomePage;
import app.pages.LikedContentPage;
import app.utils.MyConst;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Getter
public class User extends GeneralUser implements Observable {
    private final String username;
    private final int age;
    private final String city;
    private final MyConst.UserType type;
    private final HomePage homePage = new HomePage(this);
    private final LikedContentPage likedContentPage = new LikedContentPage(this);
    private String lastCommand;
    private boolean connected = true;
    private final ArrayList<Playlist> playlists = new ArrayList<>();
    private final ArrayList<Playlist> followedPlaylists = new ArrayList<>();
    private final ArrayList<Song> likedSongs = new ArrayList<>();
    private final SearchBar searchBar = new SearchBar(this);
    private final Player player = new Player(this);
    private final UserStats stats = new UserStats(this);
    private final UserIncome income = new UserIncome(this);
    private ArrayList<String> notifications = new ArrayList<>();
    private ArrayList<Page> pageHistory = new ArrayList<>();
    private int currentPageId;


    private boolean containsPlaylist(final String name) {
        for (Playlist playlist : playlists) {
            if (playlist.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a new playlist owned by user.
     *
     * @param cmd        to get playlistName
     * @param objectNode for output
     */
    public void createPlaylist(final Command cmd, final ObjectNode objectNode) {
        if (!connected) {
            standardOfflineCommand("createPlaylist", objectNode);
            return;
        }
        if (this.containsPlaylist(cmd.getPlaylistName())) {
            objectNode.put("message", "A playlist with the same name already exists.");
        } else {
            Playlist playlist = new Playlist(cmd.getPlaylistName(),
                    new ArrayList<>(),
                    this,
                    cmd.getTimestamp(),
                    true);
            playlists.add(playlist);
            objectNode.put("message", "Playlist created successfully.");
        }
    }

    /**
     * Add or remove the current playing song from my playlist.
     *
     * @param cmd        for playlistId and timestamp
     * @param objectNode for output
     */
    public void addRemoveInPlaylist(final Command cmd, final ObjectNode objectNode) {
        if (!connected) {
            standardOfflineCommand("addRemoveInPlaylist", objectNode);
            return;
        }
        int playlistId = cmd.getPlaylistId();
        if (playlistId > playlists.size() || playlistId <= 0) {
            objectNode.put("message", "The specified playlist does not exist.");
        } else if (!player.isPlaying(cmd.getTimestamp())) {
            objectNode.put("message", "Please load a source before "
                    + "adding to or removing from the playlist.");
        } else if (!(player.getSourceType() == MyConst.SourceType.SONG
                || player.getSourceType() == MyConst.SourceType.ALBUM)) {
            objectNode.put("message", "The loaded source is not a song.");
        } else {
            Song newSong = (Song) player.getTrack();
            Playlist currentPlaylist = playlists.get(playlistId - 1);
            if (currentPlaylist.containsSong(newSong)) {
                currentPlaylist.removeSong(newSong);
                objectNode.put("message", "Successfully removed from playlist.");
            } else {
                currentPlaylist.addSong(newSong);
                objectNode.put("message", "Successfully added to playlist.");
            }

        }
    }

    /**
     * Like the playing song.
     *
     * @param timestamp  for time
     * @param objectNode for output
     */
    public void like(final int timestamp, final ObjectNode objectNode) {
        if (!connected) {
            standardOfflineCommand("like", objectNode);
            return;
        }
        if (player.isPlaying(timestamp)) {
            if (player.getSongTrack(timestamp) != null) {
                Song songSource = player.getSongTrack(timestamp);
                if (likedSongs.contains(songSource)) {
                    objectNode.put("message", "Unlike registered successfully.");
                    likedSongs.remove(songSource);
                    songSource.dislike();
                } else {
                    likedSongs.add(songSource);
                    songSource.like();
                    objectNode.put("message", "Like registered successfully.");
                }
            } else {
                objectNode.put("message", "Loaded source is not a song.");
            }
        } else {
            objectNode.put("message", "Please load a source before liking or unliking.");
        }
    }

    /**
     * Switch your playlist's visibility.
     *
     * @param cmd        to get playlist Id
     * @param objectNode for output
     */
    public void switchVisibility(final Command cmd, final ObjectNode objectNode) {
        if (!connected) {
            standardOfflineCommand("switchVisibility", objectNode);
            return;
        }
        if (cmd.getPlaylistId() > playlists.size()) {
            objectNode.put("message", "The specified playlist ID is too high.");
        } else {
            Playlist selectedPlaylist = playlists.get(cmd.getPlaylistId() - 1);
            selectedPlaylist.setVisibility(!selectedPlaylist.isVisible());
            objectNode.put("message", "Visibility status updated successfully to "
                    + (selectedPlaylist.isVisible() ? "public" : "private") + ".");
        }
    }

    /**
     * Follow a public playlist playing now on player.
     *
     * @param objectNode for output.
     */
    public void followPlaylist(final ObjectNode objectNode) {
        if (!connected) {
            standardOfflineCommand("follow", objectNode);
            return;
        }
        AudioObject selectedObject = searchBar.getSelectedAudio();
        if (selectedObject == null) {
            objectNode.put("message", "Please select a source before following or unfollowing.");
        } else if (selectedObject.getType() == MyConst.SourceType.PLAYLIST) {
            Playlist selectedPlaylist = (Playlist) selectedObject;

            if (followedPlaylists.contains(selectedPlaylist)) {
                followedPlaylists.remove(selectedPlaylist);
                selectedPlaylist.unfollow(username);
                objectNode.put("message", "Playlist unfollowed successfully.");
            } else {
                if (selectedPlaylist.getOwner().equals(username)) {
                    objectNode.put("message", "You cannot follow or unfollow your own "
                            + "playlist.");
                } else {
                    followedPlaylists.add(selectedPlaylist);
                    selectedPlaylist.follow(username);
                    objectNode.put("message", "Playlist followed successfully.");
                }
            }
        } else {
            objectNode.put("message", "The selected source is not a playlist.");
        }
    }

    /**
     * Show your playlists.
     *
     * @param objectNode for output
     */
    public void showPlaylists(final ObjectNode objectNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode finalArrayNode = objectMapper.createArrayNode();
        for (Playlist playlist : playlists) {
            ObjectNode playNode = objectMapper.createObjectNode();
            playNode.put("name", playlist.getName());
            ArrayList<Song> songs = playlist.getSongs();
            ArrayNode arrayNode = objectMapper.createArrayNode();
            for (Song song : songs) {
                arrayNode.add(song.getName());
            }
            playNode.set("songs", arrayNode);
            playNode.put("visibility", (playlist.isVisible() ? "public" : "private"));
            playNode.put("followers", playlist.getFollowers());
            finalArrayNode.add(playNode);
        }
        objectNode.set("result", finalArrayNode);
    }

    /**
     * Show my liked songs.
     *
     * @param objectNode for output
     */
    public void showPreferredSongs(final ObjectNode objectNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode resultNode = objectMapper.createArrayNode();
        for (Song song : likedSongs) {
            resultNode.add(song.getName());
        }
        objectNode.set("result", resultNode);
    }

    public ArrayList<Song> getTop5LikedSongs() {
        return likedSongs
                .stream()
                .sorted((obj1, obj2) -> obj2.getLikes() - obj1.getLikes())
                .limit(MyConst.RESULT_SIZE)
                .collect(Collectors.toCollection(ArrayList::new
                ));
    }

    /**
     * Switch the connection status to the opposite than current.
     *
     * @param timestamp  the current time
     * @param objectNode for output
     */
    public void switchConnectionStatus(final int timestamp, final ObjectNode objectNode) {
        connected = !connected;
        if (connected) {
            player.unfreeze(timestamp);
        } else {
            if (player.isPlaying(timestamp)) {
                player.freeze(timestamp);
            }
        }
        objectNode.put("message", username + " has changed status successfully.");

    }

    /**
     * Fake command used for standard offline command message.
     *
     * @param command    the command type
     * @param objectNode for output
     */
    public void standardOfflineCommand(final String command, final ObjectNode objectNode) {
        objectNode.put("message", username + " is offline.");
        if (command.equals("search")) {
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode resultsNode = objectMapper.createArrayNode();
            objectNode.set("results", resultsNode);
        }

    }

    /**
     * Print the user's current page.
     *
     * @param objectNode for output
     */
    @Override
    public void printCurrentPage(final ObjectNode objectNode) {
        if (!connected) {
            this.standardOfflineCommand("printCurrentPage", objectNode);
        } else {
            super.printCurrentPage(objectNode);
        }
    }

    /**
     * Change the current page.
     *
     * @param cmd        for data
     * @param objectNode for output
     */
    public void changePage(final Command cmd, final ObjectNode objectNode) {
        String nextPage = cmd.getNextPage();
        boolean success = true;
        switch (nextPage) {
            case "Home":
                setCurrentPage(homePage);
                pageHistory.add(homePage);
                currentPageId = pageHistory.size() - 1;
                break;
            case "LikedContent":
                setCurrentPage(likedContentPage);
                pageHistory.add(likedContentPage);
                currentPageId = pageHistory.size() - 1;
                break;
            case "Host":
                if (player.isPlaying(cmd.getTimestamp())) {
                    if (player.getSource().getType() == MyConst.SourceType.PODCAST) {
                        String hostName = player.getSource().getOwner();
                        GeneralUser host = Admin.getLibrary().getUserOfType(hostName, MyConst.UserType.HOST);
                        if (host == null) {
                            success = false;
                        } else {
                            setCurrentPage(host.getCurrentPage());
                            pageHistory.add(getCurrentPage());
                            currentPageId = pageHistory.size() - 1;
                        }
                        break;
                    }
                }
                success = false;
                break;
            case "Artist":
                if (player.isPlaying(cmd.getTimestamp())) {
                    if (player.getSource().getType() != MyConst.SourceType.PODCAST) {
                        String artistName = player.getSource().getOwner();
                        GeneralUser artist = Admin.getLibrary().getUserOfType(artistName, MyConst.UserType.ARTIST);
                        if (artist == null) {
                            success = false;
                        } else {
                            setCurrentPage(artist.getCurrentPage());
                            pageHistory.add(getCurrentPage());
                            currentPageId = pageHistory.size() - 1;
                        }
                        break;
                    }
                }
                success = false;
                break;
            default:
                success = false;
                break;
        }
        if (!success) {
            objectNode.put("message", username + " is trying to access a non-existent page.");
        } else {
            objectNode.put("message", username + " accessed " + nextPage + " successfully.");
        }

    }

    public String subscribe() {
        GeneralUser channel = getCurrentPage().getOwner();
        String message = "";
        switch (channel.getType()) {
            case ARTIST, HOST -> {
                if (channel.getSubscriptions().contains(this)) {
                    removeSubscription(channel);
                    message = username + " unsubscribed from " + channel.getUsername() + " successfully.";
                } else {
                    addSubscription(channel);
                    message = username + " subscribed to " + channel.getUsername() + " successfully.";
                }
            }
            case USER -> {
                message = "To subscribe you need to be on the page of an artist or host.";
            }
        }
        return message;
    }

    public void getNotifications(final ObjectNode objectNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode1;
        ArrayNode allNotifications = objectMapper.createArrayNode();
        for (String note : notifications) {
            String[] nameAndDescription = note.split(": ");
            objectNode1 = objectMapper.createObjectNode();
            objectNode1.put("name", nameAndDescription[0]);
            objectNode1.put("description", nameAndDescription[1]);
            allNotifications.add(objectNode1);
        }
        notifications = new ArrayList<>();
        objectNode.set("notifications", allNotifications);
    }

    public void nextPage(final ObjectNode objectNode) {
        if (currentPageId == pageHistory.size() - 1) {
            objectNode.put("message", "There are no pages left to go forward.");
        } else {
            currentPageId++;
            setCurrentPage(pageHistory.get(currentPageId));
            objectNode.put("message", "The user " + username + " has navigated successfully to the next page.");
        }

    }

    public void previousPage(final ObjectNode objectNode) {
        if (currentPageId == 0) {
            objectNode.put("message", "There are no pages left to go back.");
        } else {
            currentPageId--;
            setCurrentPage(pageHistory.get(currentPageId));
            objectNode.put("message", "The user " + username + " has navigated successfully to the previous page.");
        }

    }

    public void updateRecommendations(final Command cmd, final ObjectNode objectNode) {
        player.isPlaying(cmd.getTimestamp());

        boolean success = switch (cmd.getRecommendationType()) {
            case "random_song" -> homePage.randomSongRec(player.getTrackSeek(), (Song) player.getTrack());
            case "random_playlist" -> homePage.randomPlaylistRec(cmd.getTimestamp());
            case "fans_playlist" -> homePage.fansPlaylistsRec(player.getSource().getOwner(), cmd.getTimestamp());
            default -> false;
        };
        if (success) {
            objectNode.put("message", "The recommendations for user " + username + " have been updated successfully.");
        } else {
            objectNode.put("message", "No new recommendations were found");
        }
    }

    public void loadRecommendations(final Command cmd, final ObjectNode objectNode) {
        if (!connected) {
            standardOfflineCommand("loadRecommendations", objectNode);
        }
        if (homePage.getLastRecommendation() == null) {

            objectNode.put("message", "No recommendations available.");
        } else {
            System.out.println(homePage.getLastRecommendation().getName() + homePage.getLastRecommendation().getDuration());
            player.loadSource(homePage.getLastRecommendation(), cmd.getTimestamp());
            objectNode.put("message", "Playback loaded successfully.");
        }
    }

    public User(final String username, final String city, final int age) {
        super(username, city, age, MyConst.UserType.USER);
        this.username = username;
        this.city = city;
        this.age = age;
        this.type = MyConst.UserType.USER;
        super.setCurrentPage(homePage);
    }

    /**
     * Delete user.
     * Checks if someone is using the user's stuff before deletion.
     *
     * @param library   for updating the library
     * @param timestamp the current time
     * @return true if user was deleted
     */
    @Override
    public boolean delete(final Library library, final int timestamp) {
        boolean canBeDeleted = true;
        for (User user : library.getUsers()) {
            if (user.getPlayer().isPlaying(timestamp)) {
                if (user.getPlayer().getSourceType() == MyConst.SourceType.PLAYLIST) {
                    Playlist source = (Playlist) user.getPlayer().getSource();
                    if (playlists.contains(source)) {
                        canBeDeleted = false;
                        break;
                    }
                }
            }
        }
        if (canBeDeleted) {
            library.getUsers().remove(this);
            // remove deleted user's playlists from any follower's list
            for (Playlist playlist : playlists) {
                playlist.delete(library);
            }
            for (Playlist playlist : followedPlaylists) {
                playlist.unfollow(username);
            }
            for (Song song : likedSongs) {
                song.dislike();
            }
        }
        return canBeDeleted;
    }

    /**
     * Command used to track last app.commands.
     *
     * @param lastCommand last used command
     */
    public void setLastCommand(final String lastCommand) {
        this.lastCommand = lastCommand;
    }

    @Override
    public void receiveNotification(String message) {
        notifications.add(message);
    }
}
