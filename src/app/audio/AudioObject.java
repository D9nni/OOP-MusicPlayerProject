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

    String getOwner();

    void incrementListened();

    int getListened();

}
