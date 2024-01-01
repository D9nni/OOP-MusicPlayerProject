package app.pages;

import com.fasterxml.jackson.databind.node.ObjectNode;
import app.users.GeneralUser;

public interface Page {
    /**
     * Print the page as a message line in objectNode.
     * @param objectNode for output
     */
    void printPage(ObjectNode objectNode);

    /**
     * Set the page's owner.
     * @param owner the user who owns the page
     */
    void setOwner(GeneralUser owner);
}
