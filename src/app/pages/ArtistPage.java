package app.pages;

import app.audio.Album;
import com.fasterxml.jackson.databind.node.ObjectNode;
import app.users.Artist;
import app.users.GeneralUser;
import app.users.artist_stuff.ArtistEvent;
import app.users.artist_stuff.Merch;

import java.util.ArrayList;

public final class ArtistPage implements Page {
    private Artist owner;

    @Override
    public void printPage(final ObjectNode objectNode) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Albums:\n\t[");
        String separator = ", ";
        ArrayList<Album> albums = owner.getAlbums();
        int size = albums.size();
        for (int i = 0; i < size; i++) {
            stringBuilder.append(albums.get(i).getName());
            if (i != size - 1) {
                stringBuilder.append(separator);
            }
        }

        stringBuilder.append("]\n\nMerch:\n\t[");
        ArrayList<Merch> merch = owner.getMerch();
        size = merch.size();
        for (int i = 0; i < size; i++) {
            stringBuilder.append(merch.get(i).toString());
            if (i != size - 1) {
                stringBuilder.append(separator);
            }
        }

        stringBuilder.append("]\n\nEvents:\n\t[");
        ArrayList<ArtistEvent> artistEvents = owner.getArtistEvents();
        size = artistEvents.size();
        for (int i = 0; i < size; i++) {
            stringBuilder.append(artistEvents.get(i).toString());
            if (i != size - 1) {
                stringBuilder.append(separator);
            }
        }
        stringBuilder.append("]");
        objectNode.put("message", stringBuilder.toString());
    }

    @Override
    public void setOwner(final GeneralUser owner) {
        this.owner = (Artist) owner;
    }
}
