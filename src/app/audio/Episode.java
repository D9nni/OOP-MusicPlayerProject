package app.audio;

import fileio.input.EpisodeInput;
import app.utils.MyConst;

public final class Episode extends AudioFile {
    private final EpisodeInput episodeInput;

    public Episode(final EpisodeInput episodeInput) {
        this.episodeInput = episodeInput;
    }

    /**
     *
     * @return a string in format "name - description"
     */
    @Override
    public String toString() {
        return getName() + " - " + getDescription();
    }

    @Override
    public Integer getDuration() {
        return episodeInput.getDuration();
    }
    @Override
    public MyConst.SourceType getType() {
        return MyConst.SourceType.EPISODE;
    }

    @Override
    public String getOwner() {
        return null;
    }

    @Override
    public boolean isAd() {
        return false;
    }

    public String getName() {
        return episodeInput.getName();
    }


    public String getDescription() {
        return episodeInput.getDescription();
    }

}

