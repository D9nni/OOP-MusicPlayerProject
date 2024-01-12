package app.pages;

import app.audio.Album;
import com.fasterxml.jackson.databind.node.ObjectNode;
import app.users.Artist;
import app.users.artist_stuff.ArtistEvent;
import app.users.artist_stuff.Merch;
import lombok.Getter;

import java.util.ArrayList;

@Getter
public final class ArtistPage implements Page {
    private final Artist owner;

    public ArtistPage(final Artist owner) {
        this.owner = owner;
    }


    @Override
    public void printPage(final ObjectNode objectNode) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Albums:\n\t[");
        ArrayList<Album> albums = owner.getAlbums();
        Page.appendObjectListToStringBuilder(albums, stringBuilder);

        stringBuilder.append("]\n\nMerch:\n\t[");
        ArrayList<Merch> merch = owner.getMerch();
        Page.appendObjectListToStringBuilder(merch, stringBuilder);

        stringBuilder.append("]\n\nEvents:\n\t[");
        ArrayList<ArtistEvent> artistEvents = owner.getArtistEvents();
        Page.appendObjectListToStringBuilder(artistEvents, stringBuilder);
        stringBuilder.append("]");
        objectNode.put("message", stringBuilder.toString());
    }

}
