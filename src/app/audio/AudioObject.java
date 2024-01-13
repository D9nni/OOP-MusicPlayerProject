package app.audio;

import app.utils.MyConst;

public interface AudioObject {
    /**
     *
     * @return name of AudioObject
     */
    String getName();

    /**
     *
     * @return duration of AudioObject
     */

    Integer getDuration();

    /**
     *
     * @return type of AudioObject
     */
    MyConst.SourceType getType();

    /**
     * @return owner of audio source's name
     */
    String getOwner();

    /**
     * Increment number of listens.
     */
    void incrementListened();

    /**
     * @return number of listens.
     */
    int getListened();

}
