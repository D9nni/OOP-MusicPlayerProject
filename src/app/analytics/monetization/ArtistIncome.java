package app.analytics.monetization;

import app.audio.Album;
import app.audio.Song;
import app.users.Admin;
import app.users.Artist;
import app.users.User;
import app.users.artist_stuff.Merch;
import app.utils.MyConst;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

public class ArtistIncome implements Comparable<ArtistIncome> {
    @Getter
    private final Artist artist;
    @Getter
    private double merchRevenue = 0.0;
    @Getter
    private double songRevenue = 0.0;
    private int ranking;
    private String mostProfitableSong = "N/A";

    public ArtistIncome(final Artist artist) {
        this.artist = artist;
    }

    public ObjectNode toObjectNode() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("merchRevenue", Math.round(merchRevenue * MyConst.ROUND_VALUE) / MyConst.ROUND_VALUE);
        objectNode.put("songRevenue", Math.round(songRevenue * MyConst.ROUND_VALUE) / MyConst.ROUND_VALUE);
        objectNode.put("ranking", ranking);
        objectNode.put("mostProfitableSong", mostProfitableSong);
        return objectNode;
    }

    public void sellMerch(final Merch merch) {
        merchRevenue += merch.price();
    }

    public void setRanking(final int ranking) {
        this.ranking = ranking;
    }

    @Override
    public int compareTo(final ArtistIncome o) {
        double diff = songRevenue + merchRevenue - o.getMerchRevenue() - o.getSongRevenue();
        if (diff == 0.0) {
            return artist.getUsername().compareTo(o.getArtist().getUsername());
        } else {
            if (diff < 0.0) {
                return 1;
            } else {
                return -1;
            }

        }
    }

    public void sellSongs(final Double money) {
        songRevenue += money;
    }

    public void updateMostProfitableSong() {
        HashMap<String, Double> bestSongsMap = new HashMap<>();
        for (Song song : Admin.getLibrary().getSongs()) {
            if (song.getArtist().equals(artist.getUsername()) && song.getRevenue() > 0.0d) {
                bestSongsMap.put(song.getName(), bestSongsMap.getOrDefault(song.getName(), 0.0d) + song.getRevenue());
            }
        }
        Comparator<Map.Entry<String, Double>> songComparator = Comparator
                .comparing(Map.Entry<String, Double>::getValue, Comparator.reverseOrder())
                .thenComparing(Map.Entry::getKey);
        mostProfitableSong = bestSongsMap.entrySet()
                .stream().min(songComparator).map(Map.Entry::getKey).orElse("N/A");
    }
}
