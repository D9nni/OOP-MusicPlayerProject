package app.analytics.monetization;

import app.users.Artist;
import app.users.artist_stuff.Merch;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

public class ArtistIncome implements Comparable<ArtistIncome>{
    @Getter
    private final Artist artist;
    @Getter
    private double merchRevenue = 0.0;
    @Getter
    private double songRevenue = 0.0;
    private int ranking;
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
    public void sellMerch(Merch merch) {
        merchRevenue += merch.price();
    }
    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    @Override
    public int compareTo(ArtistIncome o) {
        double diff = songRevenue + merchRevenue - o.getMerchRevenue() - o .getSongRevenue();
        if(diff == 0.0) {
            return artist.getUsername().compareTo(o.getArtist().getUsername());
        }
        else {
            if(diff < 0.0) {
                return 1;
            } else {
                return -1;
            }

        }
    }
}
