package app.analytics.wrapped;

import app.audio.Episode;
import app.audio.Podcast;
import app.users.Host;
import app.users.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class HostStats implements Wrapped {
    private final HashSet<User> fans = new HashSet<>();
    private final Host host;

    public HostStats(Host host) {
        this.host = host;
    }
    public void addFan(User user) {
        fans.add(user);
    }
    @Override
    public void wrapped(ObjectNode objectNode) {
        if(isEmpty()) {
            objectNode.put("message", Wrapped.noDataOutput(host.getUsername()));
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode1 = objectMapper.createObjectNode();
        ObjectNode objectNode2 = objectMapper.createObjectNode();
        //topEpisodes, listeners
        HashMap<Episode, Integer> allEpisodesHashMap = new HashMap<>();
        for(Podcast podcast : host.getPodcasts()) {
            Wrapped.mergeMaps(allEpisodesHashMap, Wrapped.createHashMapFromArrayList(podcast.getEpisodes()));
        }
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
}
