package app.users;

import app.analytics.wrapped.HostStats;
import app.audio.AudioFile;
import app.audio.Library;
import app.audio.Podcast;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.EpisodeInput;
import app.commands.Command;
import lombok.Getter;
import app.pages.HostPage;
import app.users.host_stuff.Announcement;
import app.utils.MyConst;

import java.util.ArrayList;

@Getter
public class Host extends GeneralUser {
    private final ArrayList<Podcast> podcasts = new ArrayList<>();
    private final ArrayList<Announcement> announcements = new ArrayList<>();
    private final HostPage hostPage = new HostPage(this);
    private final HostStats stats = new HostStats(this);


    public Host(final String username, final String city, final int age) {
        super(username, city, age, MyConst.UserType.HOST);
        super.setCurrentPage(hostPage);
    }

    /**
     * Add a new announcement.
     * @param cmd for data
     * @param objectNode for output
     */
    public void addAnnouncement(final Command cmd, final ObjectNode objectNode) {
        String name = cmd.getName();
        boolean alreadyExists = announcements.stream().anyMatch(obj -> obj.name().equals(name));
        if (alreadyExists) {
            objectNode.put("message", getUsername()
                    + " already added an announcement with this name.");
        } else {
            Announcement newAnno = new Announcement(cmd.getName(), cmd.getDescription());
            announcements.add(newAnno);
            objectNode.put("message", getUsername()
                    + " has successfully added new announcement.");
        }
    }
    /**
     * Remove an existing announcement.
     * @param cmd for data
     * @param objectNode for output
     */
    public void removeAnnouncement(final Command cmd, final ObjectNode objectNode) {
        String name = cmd.getName();
        Announcement searchedAnno = null;
        for (Announcement anno : announcements) {
            if (anno.name().equals(name)) {
                searchedAnno = anno;
                break;
            }
        }
        if (searchedAnno == null) {
            objectNode.put("message", getUsername()
                    + " has no announcement with the given name.");
        } else {
            announcements.remove(searchedAnno);
            objectNode.put("message", getUsername()
                    + " has successfully deleted the announcement.");
        }
    }
    /**
     * Add a new podcast.
     * @param library for updating the library
     * @param cmd for data
     * @param objectNode for output
     */
    public void addPodcast(final Library library, final Command cmd, final ObjectNode objectNode) {
        String name = cmd.getName();
        boolean alreadyExists = podcasts.stream().anyMatch(obj -> obj.getName().equals(name));
        if (alreadyExists) {
            objectNode.put("message", getUsername()
                    + " has another podcast with the same name.");
        } else {
            boolean twiceSame = false;
            ArrayList<EpisodeInput> episodeInputs = cmd.getEpisodes();
            for (EpisodeInput ep : episodeInputs) {
                int nrEquals = 0;
                for (EpisodeInput ep1 : episodeInputs) {
                    if (ep1.getName().equals(ep.getName())) {
                        nrEquals++;
                    }
                }
                if (nrEquals >= 2) {
                    twiceSame = true;
                    break;
                }
            }
            if (twiceSame) {
                objectNode.put("message", getUsername()
                        + " has the same episode in this podcast.");
            } else {
                Podcast podcast = new Podcast(cmd.getName(), getUsername(), episodeInputs);
                podcasts.add(podcast);
                library.getPodcasts().add(podcast); //add podcast in library list
                objectNode.put("message", getUsername()
                        + " has added new podcast successfully.");
            }
        }
    }

    /**
     * Remove an existing podcast.
     * @param library for updating the library
     * @param cmd for data
     * @param objectNode for output
     */
    public void removePodcast(final Library library, final Command cmd,
                              final ObjectNode objectNode) {
        Podcast podcastToRemove = null;
        String name = cmd.getName();
        for (Podcast podcast : podcasts) {
            if (podcast.getName().equals(name)) {
                podcastToRemove = podcast;
                break;
            }
        }
        if (podcastToRemove == null) {
            objectNode.put("message", getUsername()
                    + " doesn't have a podcast with the given name.");
        } else {
            boolean canBeRemoved = true;
            for (User user : library.getUsers()) {
                if (user.getPlayer().isPlaying(cmd.getTimestamp())) {
                    if (user.getPlayer().getSourceType() == MyConst.SourceType.PODCAST) {
                        if (user.getPlayer().getSource().equals(podcastToRemove)) {
                            canBeRemoved = false;
                        }
                    }
                }
            }
            if (!canBeRemoved) {
                objectNode.put("message", getUsername() + " can't delete this podcast.");
            } else {
                library.getPodcasts().remove(podcastToRemove);
                podcasts.remove(podcastToRemove);
                objectNode.put("message", getUsername()
                        + " deleted the podcast successfully.");
            }
        }
    }

    /**
     * Show all podcasts.
     * Shows a list with name of episodes for each podcast.
     * @param objectNode for output
     */
    public void showPodcasts(final ObjectNode objectNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode mainNode = objectMapper.createArrayNode();
        for (Podcast podcast : podcasts) {
            ObjectNode oneResult = objectMapper.createObjectNode();
            oneResult.put("name", podcast.getName());
            ArrayNode resultPodcasts = objectMapper.createArrayNode();
            for (AudioFile track : podcast.getTracks()) {
                resultPodcasts.add(track.getName());
            }
            oneResult.put("name", podcast.getName());
            oneResult.set("episodes", resultPodcasts);
            mainNode.add(oneResult);
        }
        objectNode.set("result", mainNode);
    }

    /**
     * Delete the host.
     * @param library for updating the library
     * @param timestamp the current time
     * @return true if deleted
     */
    @Override
    public boolean delete(final Library library, final int timestamp) {
        boolean canBeDeleted = true;
        for (User user : library.getUsers()) {
            if (user.getCurrentPage().equals(this.getCurrentPage())) {
                canBeDeleted = false;
                break;
            }
            if (user.getPlayer().isPlaying(timestamp)) {
                if (user.getPlayer().getSourceType() == MyConst.SourceType.PODCAST) {
                    Podcast p = (Podcast) user.getPlayer().getSource();
                    if (podcasts.contains(p)) {
                        canBeDeleted = false;
                        break;
                    }
                }
            }
        }
        if (canBeDeleted) {
            library.getHosts().remove(this);
            for (Podcast podcast : podcasts) {
                podcast.delete(library);
            }
        }
        return canBeDeleted;
    }
}
