package app.analytics.monetization;

import app.audio.Song;
import app.users.Admin;
import app.users.Artist;
import app.users.User;
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
        objectNode.put("merchRevenue", Math.round(merchRevenue*100.0)/100.0);
        objectNode.put("songRevenue", Math.round(songRevenue*100.0)/100.0);
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
    public void sellSongs(Double money) {
        songRevenue += money;
    }
    public void updateMostProfitableSong() {
        Song bestSong = null;
        Double bestRevenue = 0.0;
        for (Song song : Admin.getLibrary().getSongs()) {
            if(song.getArtist().equals(artist.getUsername())) {
                if(song.getRevenue()>bestRevenue) {
                    bestRevenue = song.getRevenue();
                    bestSong = song;
                }
            }
        }
        if(bestSong != null) {
            mostProfitableSong = bestSong.getName();
        }
    }
}
