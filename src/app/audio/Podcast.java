package app.audio;

import fileio.input.EpisodeInput;
import fileio.input.PodcastInput;
import lombok.Getter;
import app.utils.MyConst;

import java.util.ArrayList;


@Getter
public class Podcast extends AudioCollection {
    private final String name;
    private final String owner;
    private final ArrayList<Episode> episodes = new ArrayList<>();

    public Podcast(final PodcastInput podcastInput) {
        this.name = podcastInput.getName();
        this.owner = podcastInput.getOwner();
        for (EpisodeInput episodeInput : podcastInput.getEpisodes()) {
            Episode episode = new Episode(episodeInput);
            episodes.add(episode);
        }

    }

    public Podcast(final String name, final String owner, final ArrayList<EpisodeInput> episodes) {
        this.name = name;
        this.owner = owner;
        for (EpisodeInput episodeInput : episodes) {
            Episode episode = new Episode(episodeInput);
            this.episodes.add(episode);
        }
    }

    /**
     *
     * @return string in format "[episode1 - description1, episode2 - description2]"
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        int size = episodes.size();
        String separator = ", ";
        for (int i = 0; i < size; i++) {
            stringBuilder.append(episodes.get(i).toString());
            if (i != size - 1) {
                stringBuilder.append(separator);
            }
        }
        stringBuilder.append("]\n");

        return name + ":\n\t" + stringBuilder;
    }

    /**
     * Delete the podcast from library.
     * This function doesn't check if someone is playing this podcast now.
     * @param library to access podcasts list from library
     */
    public void delete(final Library library) {
        library.getPodcasts().remove(this);
    }
    /**
     * @return total duration of podcast episodes
     */
    @Override
    public Integer getDuration() {
        Integer time = 0;
        for (Episode episode : episodes) {
            time += episode.getDuration();
        }
        return time;
    }

    /**
     * @return type as String
     */
    @Override
    public MyConst.SourceType getType() {
        return MyConst.SourceType.PODCAST;
    }

    /**
     * @return episodes upcasted to AudioFiles
     */
    @Override
    public ArrayList<AudioFile> getTracks() {
        return new ArrayList<>(episodes);
    }

}
