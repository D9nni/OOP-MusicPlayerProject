package app.audio;


import app.utils.MyConst;

import java.util.ArrayList;

public abstract class AudioCollection implements AudioObject {
    private int listened = 0;
    /**
     *
     * @return name of AudioCollection
     */
    @Override
    public abstract String getName();

    /**
     *
     * @return total duration of AudioCollection
     */
    @Override
    public abstract Integer getDuration();

    /**
     *
     * @return type of class that implements AudioObject
     */
    @Override
    public abstract MyConst.SourceType getType();

    /**
     *
     * @return list of tracks to be played
     */
    public abstract ArrayList<AudioFile> getTracks();

    @Override
    public void incrementListened() {
        listened++;
    }

    @Override
    public int getListened() {
        return listened;
    }
}
