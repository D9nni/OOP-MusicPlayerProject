package app.pages;

import app.audio.Playlist;
import app.audio.Song;
import com.fasterxml.jackson.databind.node.ObjectNode;
import app.users.GeneralUser;
import app.users.User;
import app.utils.MyConst;

import java.util.ArrayList;

public final class HomePage implements Page {
    private User owner;

    public HomePage() {

    }

    @Override
    public void printPage(final ObjectNode objectNode) {
        User user = owner;
        //Liked songs:\n\t[Melodic Mirage]\n\nFollowed playlists:\n\t[]
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Liked songs:\n\t[");
        String separator = ", ";
        ArrayList<Song> likedSongs = new ArrayList<>(user.getLikedSongs());
        //user.getLikedSongs().sort((a,b) -> Integer.compare(b.getLikes(), a.getLikes()));
        likedSongs.sort((a, b) -> Integer.compare(b.getLikes(), a.getLikes()));
        int size = Integer.min(likedSongs.size(), MyConst.RESULT_SIZE);
        for (int i = 0; i < size; i++) {
            stringBuilder.append(likedSongs.get(i).getName());
            if (i != size - 1) {
                stringBuilder.append(separator);
            }
        }

        stringBuilder.append("]\n\nFollowed playlists:\n\t[");
        ArrayList<Playlist> followedPlaylists = new ArrayList<>(user.getFollowedPlaylists());
        followedPlaylists.sort((a, b) -> Integer.compare(b.calculateLikes(), a.calculateLikes()));
        size = Integer.min(followedPlaylists.size(), MyConst.RESULT_SIZE);
        for (int i = 0; i < size; i++) {
            stringBuilder.append(followedPlaylists.get(i).getName());
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
