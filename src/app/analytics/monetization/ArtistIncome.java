package app.analytics.monetization;

import app.users.Artist;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ArtistIncome {
    private final Artist artist;
    private double merchRevenue = 0.0;
    private double songRevenue = 0.0;
    private int ranking = 1;
    private String mostProfitableSong = "N/A";

    public ArtistIncome(Artist artist) {
        this.artist = artist;
    }
    public ObjectNode toObjectNode() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("merchRevenue", merchRevenue);
        objectNode.put("songRevenue", songRevenue);
        objectNode.put("ranking", ranking);
        objectNode.put("mostProfitableSong", mostProfitableSong);
        return objectNode;
    }
}
