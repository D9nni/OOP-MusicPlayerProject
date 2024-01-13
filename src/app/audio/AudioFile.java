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

    /**
     * Increment number of listens.
     */
    @Override
    public void incrementListened() {
        listened++;
    }

    /**
     * @return number of listens
     */
    @Override
    public int getListened() {
        return listened;
    }

    /**
     * Check if a song is Ad
     * @return true if ad
     */

    public abstract boolean isAd();
}
