package app.analytics.monetization;

import app.audio.Song;
import app.users.Artist;
import app.users.GeneralUser;
import app.users.User;
import app.users.artist_stuff.Merch;
import app.utils.MyConst;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;

public class UserIncome {
    private final User user;
    private final ArrayList<Merch> boughtMerch = new ArrayList<>();
    @Getter
    private HashMap<Song, Integer> premiumSongs;
    @Getter
    private HashMap<Song, Integer> nonPremiumSongs = new HashMap<>();
    @Getter
    private HashMap<Artist, Integer> premiumArtists;
    @Getter
    private HashMap<Artist, Integer> nonPremiumArtists = new HashMap<>();
    @Getter
    private boolean premium = false;
    private Song lastAd = null;

    public UserIncome(final User user) {
        this.user = user;
    }

    public void buyMerch(final ObjectNode objectNode, final String merchName) {
        GeneralUser pageOwner = user.getCurrentPage().getOwner();
        if (pageOwner.getType() == MyConst.UserType.ARTIST) {
            Artist artist = (Artist) pageOwner;
            Merch selectedMerch = null;
            for (Merch merch : artist.getMerch()) {
                if (merch.name().equals(merchName)) {
                    selectedMerch = merch;
                    break;
                }
            }
            if (selectedMerch != null) {
                boughtMerch.add(selectedMerch);
                artist.getIncome().sellMerch(selectedMerch);
                objectNode.put("message", user.getUsername() + " has added new merch successfully.");
            } else {
                objectNode.put("message", "The merch " + merchName + " doesn't exist.");
            }
        } else {
            objectNode.put("message", "Cannot buy merch from this page.");
        }
    }

    public void seeMerch(final ObjectNode objectNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode merchNode = objectMapper.createArrayNode();
        for (Merch merch : boughtMerch) {
            merchNode.add(merch.name());
        }
        objectNode.set("result", merchNode);
    }

    public void buyPremium(final ObjectNode objectNode) {
        if (premium) {
            objectNode.put("message", user.getUsername() + " is already a premium user.");
        } else {
            paySongs();
            premium = true;
            premiumSongs = new HashMap<>();
            premiumArtists = new HashMap<>();
            objectNode.put("message", user.getUsername() + " bought the subscription successfully.");
        }
    }

    public void cancelPremium(final ObjectNode objectNode) {
        if (premium) {
            paySongs();
            premium = false;
            premiumSongs = new HashMap<>();
            premiumArtists = new HashMap<>();
            objectNode.put("message", user.getUsername() + " cancelled the subscription successfully.");
        } else {
            objectNode.put("message", user.getUsername() + " is not a premium user.");
        }
    }

    public void updateMonetizationSongs(final Song song) {
        if (premium) {
                premiumSongs.put(song, premiumSongs.getOrDefault(song, 0) + 1);
        } else {
            if (song.isAd()) {
                lastAd = song;
                paySongs();
                lastAd = null;
                nonPremiumSongs = new HashMap<>();
                nonPremiumArtists = new HashMap<>();
            } else {
                nonPremiumSongs.put(song, nonPremiumSongs.getOrDefault(song, 0) + 1);
            }
        }
    }

    public void updateMonetizationArtists(final Artist artist) {
        if (premium) {
            premiumArtists.put(artist, premiumArtists.getOrDefault(artist, 0) + 1);
        } else {
            nonPremiumArtists.put(artist, nonPremiumArtists.getOrDefault(artist, 0) + 1);
        }
    }

    public void paySongs() {
        if (!premium) {
            if (lastAd != null) {
                paySongsHelper((double) lastAd.getPrice(), nonPremiumSongs, nonPremiumArtists);
            }
        } else {
            paySongsHelper(MyConst.USER_CREDIT, premiumSongs, premiumArtists);
        }

    }

    private void paySongsHelper(final Double totalMoney, final HashMap<Song, Integer> songsList,
                                final HashMap<Artist, Integer> artistsList) {
        int totalSongs = 0;
        for (Song song : songsList.keySet()) {
            totalSongs += songsList.get(song);
        }
        for (Song song : songsList.keySet()) {
            song.addRevenue(totalMoney * songsList.get(song) / totalSongs);
        }
        for (Artist artist : artistsList.keySet()) {
            int listenedSongs = artistsList.get(artist);
            Double moneyToPay = totalMoney * listenedSongs / totalSongs;
            artist.getIncome().sellSongs(moneyToPay);
        }
    }
}
