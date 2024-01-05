package app.commands;

import app.analytics.wrapped.UserStats;
import app.audio.Library;
import app.users.GeneralUser;
import app.users.User;
import app.users.Host;
import app.users.Artist;
import app.users.Admin;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import app.utils.MyConst;

import java.util.ArrayList;

public final class CommandManager {
    private final ArrayList<Command> commandArrayList;
    private final Library library;
    private final ArrayNode outputs;
    private final ObjectMapper objectMapper;

    private static CommandManager instance;

    private CommandManager(final ArrayList<Command> commandArrayList, final Library library,
                           final ArrayNode outputs, final ObjectMapper objectMapper) {
        this.commandArrayList = commandArrayList;
        this.library = library;
        Admin.setLibrary(library);
        this.outputs = outputs;
        this.objectMapper = objectMapper;
    }

    /**
     * Get the unique instance of Singleton class CommandManager
     * @param commandArrayList list of commands to run
     * @param library          application's library
     * @param outputs          for writing output
     * @param objectMapper     for output
     * @return the single instance of CommandManager
     */
    public static CommandManager getInstance(final ArrayList<Command> commandArrayList,
                                             final Library library,
                                             final ArrayNode outputs,
                                             final ObjectMapper objectMapper) {
        if (null == instance) {
            instance = new CommandManager(commandArrayList, library, outputs, objectMapper);
        }
        return instance;
    }

    /**
     * Destroy the CommandManager single instance after executing commands
     */
    private void destroyInstance() {
        instance = null;
    }

    /**
     * Main command that checks which command is going to be run
     */
    public void executeCommands() {
        UserStats.setLibrary(library);
        for (Command cmd : commandArrayList) {
            if(cmd.getTimestamp() == 833559) {
                int salut = 1; // atergeta
            }

            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("command", cmd.getCommand());
            boolean valid = true;
            //this will happen for statistics
            if (cmd.getUsername() == null) {
                objectNode.put("timestamp", cmd.getTimestamp());
                valid = statisticCommandsSwitch(cmd, objectNode);
            } else {
                objectNode.put("user", cmd.getUsername());
                objectNode.put("timestamp", cmd.getTimestamp());
                // this switch is used for app.commands that doesn't depend on user
                switch (cmd.getCommand()) {
                    case "switchConnectionStatus" -> Admin.trySwitchConnectionStatus(cmd,
                            objectNode);
                    case "addUser" -> Admin.tryAddUser(cmd, objectNode);
                    case "deleteUser" -> Admin.tryDeleteUser(cmd, objectNode);
                    case "addAlbum" -> Admin.tryAddAlbum(cmd, objectNode);
                    case "addEvent" -> Admin.tryAddEvent(cmd, objectNode);
                    case "removeEvent" -> Admin.tryRemoveEvent(cmd, objectNode);
                    case "addMerch" -> Admin.tryAddMerch(cmd, objectNode);
                    case "addAnnouncement" -> Admin.tryAddAnnouncement(cmd, objectNode);
                    case "removeAnnouncement" -> Admin.tryRemoveAnnouncement(cmd, objectNode);
                    case "addPodcast" -> Admin.tryAddPodcast(cmd, objectNode);
                    case "removePodcast" -> Admin.tryRemovePodcast(cmd, objectNode);
                    case "removeAlbum" -> Admin.tryRemoveAlbum(cmd, objectNode);
                    case "wrapped" -> Admin.tryWrapped(cmd, objectNode);
                    // for app.commands that depend on user
                    default -> {
                        GeneralUser genUser = library.getGeneralUser(cmd.getUsername());
                        if (genUser != null) {
                            if (genUser.getType() == MyConst.UserType.USER) {
                                User user = (User) genUser;
                                // switch for normal user app.commands
                                valid = userCommandsSwitch(user, cmd, objectNode);
                            } else if (genUser.getType() == MyConst.UserType.ARTIST) {
                                Artist artist = (Artist) genUser;
                                // switch for artist app.commands that never fail
                                valid = artistCommandsSwitch(artist, cmd, objectNode);
                            } else if (genUser.getType() == MyConst.UserType.HOST) {
                                Host host = (Host) genUser;
                                // switch for host app.commands that never fail
                                valid = hostCommandsSwitch(host, cmd, objectNode);
                            }
                        }
                    }
                }
            }
            if (valid) {
                outputs.add(objectNode);
            } else {
                System.out.println("Invalid command " + cmd.getCommand());
            }
        }
        outputs.add(library.endProgram());
        destroyInstance();
    }
    private boolean userCommandsSwitch(final User user, final Command cmd,
                                    final ObjectNode objectNode) {
        switch (cmd.getCommand()) {
            // app.commands for searchBar
            case "search" -> {
                user.getSearchBar().search(cmd, library, objectNode);
                user.getPlayer().unload(cmd.getTimestamp());
                user.setLastCommand(cmd.getCommand());
            }
            case "select" -> user.getSearchBar().select(cmd, objectNode);
            // app.commands for player
            case "load" -> user.getPlayer().load(cmd, objectNode);
            case "playPause" -> user.getPlayer().playPause(cmd.getTimestamp(), objectNode);
            case "status" -> user.getPlayer().status(cmd, objectNode);
            case "repeat" -> user.getPlayer().repeat(cmd, objectNode);
            case "shuffle" -> user.getPlayer().shuffle(cmd, objectNode);
            case "next" -> user.getPlayer().next(cmd, objectNode);
            case "prev" -> user.getPlayer().prev(cmd, objectNode);
            case "forward" -> user.getPlayer().forward(cmd, objectNode);
            case "backward" -> user.getPlayer().backward(cmd, objectNode);
            // app.commands for user
            case "createPlaylist" -> user.createPlaylist(cmd, objectNode);
            case "addRemoveInPlaylist" -> user.addRemoveInPlaylist(cmd, objectNode);
            case "switchVisibility" -> user.switchVisibility(cmd, objectNode);
            case "follow" -> user.followPlaylist(objectNode);
            case "like" -> user.like(cmd.getTimestamp(), objectNode);
            case "showPlaylists" -> user.showPlaylists(objectNode);
            case "showPreferredSongs" -> user.showPreferredSongs(objectNode);
            case "printCurrentPage" -> user.printCurrentPage(objectNode);
            case "changePage" -> user.changePage(cmd, objectNode);
            default -> {
                return false;
            }
        }
        return true;
    }
    private boolean statisticCommandsSwitch(final Command cmd, final ObjectNode objectNode) {
        switch (cmd.getCommand()) {
            case "getTop5Playlists" -> library.getTop5Playlists(objectNode);
            case "getTop5Songs" -> library.getTop5Songs(objectNode);
            case "getTop5Albums" -> library.getTop5Albums(objectNode);
            case "getTop5Artists" -> library.getTop5Artists(objectNode);
            case "getOnlineUsers" -> library.getOnlineUsers(objectNode);
            case "getAllUsers" -> library.getAllUsers(objectNode);
            default -> {
                return false;
            }
        }
        return true;
    }
    private boolean artistCommandsSwitch(final Artist artist, final Command cmd,
                                      final ObjectNode objectNode) {
        switch (cmd.getCommand()) {
            case "showAlbums" -> artist.showAlbums(objectNode);
            default -> {
                return false;
            }
        }
        return true;
    }
    private boolean hostCommandsSwitch(final Host host, final Command cmd,
                                    final ObjectNode objectNode) {
        switch (cmd.getCommand()) {
            case "showPodcasts" -> host.showPodcasts(objectNode);
            default -> {
                return false;
            }
        }
        return true;
    }

}
