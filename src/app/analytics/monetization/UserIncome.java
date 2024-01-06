package app.analytics.monetization;

import app.analytics.wrapped.Wrapped;
import app.audio.Library;
import app.audio.Song;
import app.users.Admin;
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
    private HashMap<Artist, Integer> premiumArtists;
    @Getter
    private boolean premium = false;

    public UserIncome(User user) {
        this.user = user;
    }

    public void buyMerch(ObjectNode objectNode, String merchName) {
        GeneralUser pageOwner = user.getCurrentPage().getOwner();
        if(pageOwner.getType() == MyConst.UserType.ARTIST) {
            Artist artist = (Artist) pageOwner;
            Merch selectedMerch = null;
            for (Merch merch : artist.getMerch()) {
                if(merch.name().equals(merchName)) {
                    selectedMerch = merch;
                    break;
                }
            }
            if(selectedMerch != null){
                boughtMerch.add(selectedMerch);
                artist.getIncome().sellMerch(selectedMerch);
                objectNode.put("message", user.getUsername() + " has added new merch successfully.");
            } else {
                objectNode.put("message", "The merch "+ merchName +" doesn't exist.");
            }
        } else {
            objectNode.put("message","Cannot buy merch from this page.");
        }
    }
    public void seeMerch(ObjectNode objectNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode merchNode = objectMapper.createArrayNode();
        for (Merch merch : boughtMerch) {
            merchNode.add(merch.name());
        }
        objectNode.set("result", merchNode);
    }

    public void buyPremium(ObjectNode objectNode) {
        if (premium) {
            objectNode.put("message", user.getUsername() + " is already a premium user.");
        } else {
            premium = true;
            premiumSongs = new HashMap<>();
            premiumArtists = new HashMap<>();
            objectNode.put("message", user.getUsername() + " bought the subscription successfully.");
        }
    }

    public void cancelPremium(ObjectNode objectNode) {
        if(premium) {
            paySongs();
            premium = false;
            objectNode.put("message", user.getUsername() + " cancelled the subscription successfully.");
        } else {
            objectNode.put("message", user.getUsername() + " is not a premium user.");
        }
    }
    public void updatePremiumSongs(Song song) {
        if(premium) {
            premiumSongs.put(song, premiumSongs.getOrDefault(song, 0) + 1);
        }
    }
    public void updatePremiumArtists(Artist artist) {
        if(premium) {
            premiumArtists.put(artist, premiumArtists.getOrDefault(artist,0) + 1);
        }
    }
    public void paySongs() {
        if(!premium) {
            return;
        }
        HashMap <Song, Integer> uniqueSongs = Wrapped.mergeDuplicateSongs(premiumSongs);
        int totalSongs = 0;
        for (Song song : uniqueSongs.keySet()) {
            totalSongs += uniqueSongs.get(song);
        }
        for (Song song : uniqueSongs.keySet()) {
            song.addRevenue(MyConst.USER_CREDIT*uniqueSongs.get(song) / totalSongs);
        }
        for (Artist artist : premiumArtists.keySet()) {
            int listenedSongs = premiumArtists.get(artist);
            Double moneyToPay = MyConst.USER_CREDIT * listenedSongs / totalSongs;
            artist.getIncome().sellSongs(moneyToPay);
        }
    }
}
