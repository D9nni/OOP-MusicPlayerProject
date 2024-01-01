package app.pages;

import app.audio.Playlist;
import app.audio.Song;
import com.fasterxml.jackson.databind.node.ObjectNode;
import app.users.GeneralUser;
import app.users.User;
import java.util.ArrayList;

public final class LikedContentPage implements Page {
    private User owner;

    public LikedContentPage() {

    }

    @Override
    public void printPage(final ObjectNode objectNode) {
        User user = owner;
        //Liked songs:\n\t[Melodic Mirage]\n\nFollowed playlists:\n\t[]
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Liked songs:\n\t[");
        String separator = ", ";
        ArrayList<Song> likedSongs = user.getLikedSongs();
        int size = likedSongs.size();
        for (int i = 0; i < size; i++) {
            stringBuilder.append(likedSongs.get(i).toString());
            if (i != size - 1) {
                stringBuilder.append(separator);
            }
        }

        stringBuilder.append("]\n\nFollowed playlists:\n\t[");
        ArrayList<Playlist> followedPlaylists = user.getFollowedPlaylists();
        size = followedPlaylists.size();
        for (int i = 0; i < size; i++) {
            stringBuilder.append(followedPlaylists.get(i).toString());
            if (i != size - 1) {
                stringBuilder.append(separator);
            }
        }
        stringBuilder.append("]");
        objectNode.put("message", stringBuilder.toString());

    }

    @Override
    public void setOwner(final GeneralUser owner) {
        this.owner = (User) owner;
    }
}
