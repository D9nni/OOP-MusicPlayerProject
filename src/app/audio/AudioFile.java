package app.audio;

import app.utils.MyConst;

public abstract class AudioFile implements AudioObject {
    private int listened = 0;
    /**
     *
     * @return name of AudioFile
     */
    @Override
    public abstract String getName();

    /**
     *
     * @return duration of AudioFile
     */
    @Override
    public abstract Integer getDuration();

    /**
     *
     * @return type of AudioFile
     */
    @Override
    public abstract MyConst.SourceType getType();

    @Override
    public void incrementListened() {
        listened++;
    }

    @Override
    public int getListened() {
        return listened;
    }

    public abstract boolean isAd();
}
