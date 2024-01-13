package app.pages;

import app.audio.AudioObject;
import app.utils.MyConst;
import com.fasterxml.jackson.databind.node.ObjectNode;
import app.users.GeneralUser;

import java.util.ArrayList;

public interface Page {
    /**
     * Print the page as a message line in objectNode.
     * @param objectNode for output
     */
    void printPage(ObjectNode objectNode);

    /**
     * @return the user who owns the page
     */
    GeneralUser getOwner();

    /**
     * Append all audio objects to stringBuilder using method getName().
     * @param audioList list of audio objects
     * @param stringBuilder the string builder
     * @param <K> any audio object type
     */
    static <K extends AudioObject> void appendAudioListToStringBuilder(
            ArrayList<K> audioList, StringBuilder stringBuilder) {
        int size = Integer.min(audioList.size(), MyConst.RESULT_SIZE);
        for (int i = 0; i < size; i++) {
            stringBuilder.append(audioList.get(i).getName());
            if (i != size - 1) {
                stringBuilder.append(", ");
            }
        }
    }

    /**
     * Append all objects to string builder using method toString().
     * @param objectList list of objects
     * @param stringBuilder the string builder
     * @param <K> any type with method toString()
     */
    static <K> void appendObjectListToStringBuilder(
            ArrayList<K> objectList, StringBuilder stringBuilder) {
        int size = objectList.size();
        for (int i = 0; i < size; i++) {
            stringBuilder.append(objectList.get(i).toString());
            if (i != size - 1) {
                stringBuilder.append(", ");
            }
        }
    }
}
