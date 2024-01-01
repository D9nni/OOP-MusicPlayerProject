package app.users;

import app.audio.Library;
import com.fasterxml.jackson.databind.node.ObjectNode;
import app.commands.Command;
import lombok.Getter;
import app.pages.Page;
import app.utils.MyConst;

@Getter
public class GeneralUser {
    private final String username;
    private final int age;
    private final String city;
    private final MyConst.UserType type;
    private Page currentPage;

    public GeneralUser(final String username, final String city,
                       final int age, final MyConst.UserType type) {
        this.username = username;
        this.city = city;
        this.age = age;
        this.type = type;
    }

    /**
     * Delete factory. Check the user's type then calls his specific delete function.
     * @param library for updating the library
     * @param timestamp the current time
     * @return true if it was deleted
     */
    public boolean delete(final Library library, final int timestamp) {
        return switch (type) {
            case USER -> {
                User user = (User) this;
                yield user.delete(library, timestamp);
            }
            case ARTIST -> {
                Artist artist = (Artist) this;
                yield artist.delete(library, timestamp);
            }

            case HOST -> {
                Host host = (Host) this;
                yield host.delete(library, timestamp);
            }
        };
    }

    /**
     * Create app.users factory. Checks the desired user type then creates it.
     * @param library for updating the library
     * @param cmd for data
     * @return true if it created a new user
     */
    public static boolean createUser(final Library library, final Command cmd) {
        String username = cmd.getUsername();
        boolean created = true;
        if (library.generalUserExists(username)) {
            created = false;
        } else {
            String type = cmd.getType();
            switch (type) {
                case "artist":
                    Artist newArtist = new Artist(username, cmd.getCity(), cmd.getAge());
                    library.addArtist(newArtist);
                    break;
                case "host":
                    Host newHost = new Host(username, cmd.getCity(), cmd.getAge());
                    library.addHost(newHost);
                    break;
                default:
                    User newUser = new User(username, cmd.getCity(), cmd.getAge());
                    library.addUser(newUser);
                    break;
            }
        }
        return created;
    }

    /**
     * Print the user's current page.
     * Works like a visit command.
     * @param objectNode for output
     */
    public void printCurrentPage(final ObjectNode objectNode) {
        currentPage.printPage(objectNode);
    }

    /**
     * Change the user's current page.
     * @param currentPage the new page
     */
    public void setCurrentPage(final Page currentPage) {
        this.currentPage = currentPage;
    }

}
