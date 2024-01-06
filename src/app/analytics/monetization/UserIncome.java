package app.analytics.monetization;

import app.users.Artist;
import app.users.GeneralUser;
import app.users.User;
import app.users.artist_stuff.Merch;
import app.utils.MyConst;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;

public class UserIncome {
    private final User user;
    ArrayList<Merch> boughtMerch = new ArrayList<>();

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
}
