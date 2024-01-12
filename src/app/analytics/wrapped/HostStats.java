package app.analytics.wrapped;

import app.audio.Episode;
import app.users.Host;
import app.users.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class HostStats implements Wrapped {
    private final HashSet<User> fans = new HashSet<>();
    private final HashSet<Episode> episodes = new HashSet<>();
    private final Host host;

    public HostStats(final Host host) {
        this.host = host;
    }

    public void addFan(final User user) {
        fans.add(user);
    }

    @Override
    public void wrapped(final ObjectNode objectNode) {
        if (isEmpty()) {
            objectNode.put("message", "No data to show for host " + host.getUsername() + ".");
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode1 = objectMapper.createObjectNode();
        ObjectNode objectNode2 = objectMapper.createObjectNode();
        //topEpisodes, listeners
        HashMap<Episode, Integer> allEpisodesHashMap = Wrapped.createHashMapFromArrayList(
                new ArrayList<>(episodes));
        LinkedHashMap<Episode, Integer> episodesResults = Wrapped.createResults(allEpisodesHashMap, episodeComparator);

        for (Episode episode : episodesResults.keySet()) {
            objectNode2.put(episode.getName(), episodesResults.get(episode));
        }
        objectNode1.set("topEpisodes", objectNode2);

        objectNode1.put("listeners", fans.size());

        objectNode.set("result", objectNode1);
    }

    @Override
    public boolean isEmpty() {
        return fans.isEmpty();
    }

    public void addEpisode(final Episode episode) {
        episodes.add(episode);
    }
}
