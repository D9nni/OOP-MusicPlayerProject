package app.users;

import app.audio.Library;
import com.fasterxml.jackson.databind.node.ObjectNode;
import app.commands.Command;
import app.utils.MyConst;
import lombok.Getter;


public final class Admin {
    @Getter
    private static Library library;

    private Admin() {

    }

    /**
     * Gets a user of some type or writes in objectNode a message.
     * @param userType the type of user expected to be used in a command
     * @param cmd for username and timestamp
     * @param objectNode for output
     * @return null if no user of requested type was found and output was written.
     * Else returns the expected user.
     */
    private static GeneralUser getUserOfTypeOrWriteMessage(final MyConst.UserType userType,
                                                           final Command cmd,
                                                           final ObjectNode objectNode) {
        String username = cmd.getUsername();
        GeneralUser genUser = library.getGeneralUser(cmd.getUsername());
        objectNode.put("user", username);
        objectNode.put("timestamp", cmd.getTimestamp());

        if (genUser == null) {
            objectNode.put("message", "The username " + username + " doesn't exist.");
        } else if (genUser.getType() != userType) {
            String expectedType = switch (userType) {
                case USER -> "a normal user.";
                case ARTIST -> "an artist.";
                case HOST -> "a host.";
            };
            objectNode.put("message", username + " is not " + expectedType);
            genUser = null;
        }
        return genUser;
    }

    /**
     * Try to switch the connection status.
     * Will succeed only on a normal user.
     * @param cmd for data
     * @param objectNode for output
     */
    public static void trySwitchConnectionStatus(final Command cmd, final ObjectNode objectNode) {
        GeneralUser genUser = getUserOfTypeOrWriteMessage(MyConst.UserType.USER, cmd, objectNode);
        if (genUser != null) {
            User user = (User) genUser;
            user.switchConnectionStatus(cmd.getTimestamp(), objectNode);
        }
    }

    /**
     * Try to add a new album.
     * Will succeed only if the user is an artist.
     * @param cmd for data
     * @param objectNode for output
     */
    public static void tryAddAlbum(final Command cmd, final ObjectNode objectNode) {
        GeneralUser user = getUserOfTypeOrWriteMessage(MyConst.UserType.ARTIST, cmd, objectNode);
        if (user != null) {
            Artist artist = (Artist) user;
            artist.addAlbum(library, cmd, objectNode);
        }
    }

    /**
     * Try to add a new event.
     * Will succeed only if the user is an artist.
     * @param cmd for data
     * @param objectNode for output
     */
    public static void tryAddEvent(final Command cmd, final ObjectNode objectNode) {
        GeneralUser user = getUserOfTypeOrWriteMessage(MyConst.UserType.ARTIST, cmd, objectNode);
        if (user != null) {
            Artist artist = (Artist) user;
            artist.addEvent(cmd, objectNode);
        }
    }
    /**
     * Try to add a new merch.
     * Will succeed only if the user is an artist.
     * @param cmd for data
     * @param objectNode for output
     */
    public static void tryAddMerch(final Command cmd, final ObjectNode objectNode) {
        GeneralUser user = getUserOfTypeOrWriteMessage(MyConst.UserType.ARTIST, cmd, objectNode);
        if (user != null) {
            Artist artist = (Artist) user;
            artist.addMerch(cmd, objectNode);
        }
    }
    /**
     * Try to add a new announcement.
     * Will succeed only if the user is a host.
     * @param cmd for data
     * @param objectNode for output
     */
    public static void tryAddAnnouncement(final Command cmd, final ObjectNode objectNode) {
        GeneralUser user = getUserOfTypeOrWriteMessage(MyConst.UserType.HOST, cmd, objectNode);
        if (user != null) {
            Host host = (Host) user;
            host.addAnnouncement(cmd, objectNode);
        }
    }
    /**
     * Try to remove an announcement.
     * Will succeed only if the user is a host and the announcement exists.
     * @param cmd for data
     * @param objectNode for output
     */
    public static void tryRemoveAnnouncement(final Command cmd, final ObjectNode objectNode) {
        GeneralUser user = getUserOfTypeOrWriteMessage(MyConst.UserType.HOST, cmd, objectNode);
        if (user != null) {
            Host host = (Host) user;
            host.removeAnnouncement(cmd, objectNode);
        }
    }
    /**
     * Try to add a new podcast.
     * Will succeed only if the user is a host.
     * @param cmd for data
     * @param objectNode for output
     */
    public static void tryAddPodcast(final Command cmd, final ObjectNode objectNode) {
        GeneralUser user = getUserOfTypeOrWriteMessage(MyConst.UserType.HOST, cmd, objectNode);
        if (user != null) {
            Host host = (Host) user;
            host.addPodcast(library, cmd, objectNode);
        }
    }
    /**
     * Try to remove a podcast.
     * Will succeed only if the user is a host and the podcast exists.
     * @param cmd for data
     * @param objectNode for output
     */
    public static void tryRemovePodcast(final Command cmd, final ObjectNode objectNode) {
        GeneralUser user = getUserOfTypeOrWriteMessage(MyConst.UserType.HOST, cmd, objectNode);
        if (user != null) {
            Host host = (Host) user;
            host.removePodcast(library, cmd, objectNode);
        }
    }
    /**
     * Try to remove an album.
     * Will succeed only if the user is an artist and the album exists.
     * @param cmd for data
     * @param objectNode for output
     */
    public static void tryRemoveAlbum(final Command cmd, final ObjectNode objectNode) {
        GeneralUser user = getUserOfTypeOrWriteMessage(MyConst.UserType.ARTIST, cmd, objectNode);
        if (user != null) {
            Artist artist = (Artist) user;
            artist.removeAlbum(library, cmd, objectNode);
        }
    }
    /**
     * Try to remove an event.
     * Will succeed only if the user is an artist and the event exists.
     * @param cmd for data
     * @param objectNode for output
     */
    public static void tryRemoveEvent(final Command cmd, final ObjectNode objectNode) {
        GeneralUser user = getUserOfTypeOrWriteMessage(MyConst.UserType.ARTIST, cmd, objectNode);
        if (user != null) {
            Artist artist = (Artist) user;
            artist.removeEvent(cmd, objectNode);
        }
    }
    /**
     * Try to add a new user.
     * Will succeed only if the given username isn't registered as user.
     * @param cmd for data
     * @param objectNode for output
     */
    public static void tryAddUser(final Command cmd, final ObjectNode objectNode) {
        String username = cmd.getUsername();
        boolean created = GeneralUser.createUser(library, cmd);
        if (!created) {
            objectNode.put("message", "The username " + username + " is already taken.");
        } else {
            objectNode.put("message", "The username " + username + " has been added successfully.");
        }
    }
    /**
     * Try to delete a user.
     * Will succeed only if the user exists and no one is using his stuff now.
     * @param cmd for data
     * @param objectNode for output
     */
    public static void tryDeleteUser(final Command cmd, final ObjectNode objectNode) {
        String username = cmd.getUsername();
        GeneralUser generalUser = library.getGeneralUser(username);
        if (generalUser == null) {
            objectNode.put("message", " The username " + username + " doesn't exist.");
        } else {
            boolean wasDeleted = generalUser.delete(library, cmd.getTimestamp());
            if (!wasDeleted) {
                objectNode.put("message", username + " can't be deleted.");
            } else {
                objectNode.put("message", username + " was successfully deleted.");
            }
        }
    }

    public static void tryWrapped(final Command cmd, final ObjectNode objectNode) {
        String username = cmd.getUsername();
        GeneralUser generalUser = library.getGeneralUser(username);
        if (generalUser == null) {
            objectNode.put("message", "No data to show for user " + username + ".");
        } else {
            for (User user : library.getUsers()) {
                user.getPlayer().isPlaying(cmd.getTimestamp()); //update all players
            }
            generalUser.getStats().wrapped(objectNode);
        }
    }
    public static void tryBuyMerch(final Command cmd, final ObjectNode objectNode) {
        String username = cmd.getUsername();
        GeneralUser generalUser = library.getUserOfType(username, MyConst.UserType.USER);
        if (generalUser == null) {
            objectNode.put("message", " The username " + username + " doesn't exist.");
        } else {
            User user = (User) generalUser;
            user.getIncome().buyMerch(objectNode, cmd.getName());
        }
    }
    public static void trySeeMerch(final Command cmd, final ObjectNode objectNode) {
        String username = cmd.getUsername();
        GeneralUser generalUser = library.getUserOfType(username, MyConst.UserType.USER);
        if (generalUser == null) {
            objectNode.put("message", " The username " + username + " doesn't exist.");
        } else {
            User user = (User) generalUser;
            user.getIncome().seeMerch(objectNode);
        }
    }

    public static void tryBuyPremium(final Command cmd, final ObjectNode objectNode) {
        String username = cmd.getUsername();
        GeneralUser generalUser = library.getUserOfType(username, MyConst.UserType.USER);
        if (generalUser == null) {
            objectNode.put("message", " The username " + username + " doesn't exist.");
        } else {
            User user = (User) generalUser;
            user.getIncome().buyPremium(objectNode);
        }
    }
    public static void tryCancelPremium(final Command cmd, final ObjectNode objectNode) {
        String username = cmd.getUsername();
        GeneralUser generalUser = library.getUserOfType(username, MyConst.UserType.USER);
        if (generalUser == null) {
            objectNode.put("message", " The username " + username + " doesn't exist.");
        } else {
            User user = (User) generalUser;
            user.getIncome().cancelPremium(objectNode);
        }
    }

    public static void tryAdBreak(final Command cmd, final ObjectNode objectNode) {
        String username = cmd.getUsername();
        GeneralUser generalUser = library.getUserOfType(username, MyConst.UserType.USER);
        if (generalUser == null) {
            objectNode.put("message", " The username " + username + " doesn't exist.");
        } else {
            User user = (User) generalUser;
            user.getPlayer().adBreak(cmd.getTimestamp(), objectNode);
        }
    }

    public static void setLibrary(final Library library) {
        Admin.library = library;
    }
}
