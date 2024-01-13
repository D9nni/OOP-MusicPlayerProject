package app.pages;

import app.audio.Podcast;
import com.fasterxml.jackson.databind.node.ObjectNode;
import app.users.Host;
import app.users.host_stuff.Announcement;
import lombok.Getter;

import java.util.ArrayList;

@Getter
public final class HostPage implements Page {
    private final Host owner;

    public HostPage(final Host owner) {
        this.owner = owner;
    }

    @Override
    public void printPage(final ObjectNode objectNode) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Podcasts:\n\t[");
        ArrayList<Podcast> podcasts = owner.getPodcasts();
        Page.appendObjectListToStringBuilder(podcasts, stringBuilder);

        stringBuilder.append("]\n\nAnnouncements:\n\t[");
        ArrayList<Announcement> announcements = owner.getAnnouncements();
        Page.appendObjectListToStringBuilder(announcements, stringBuilder);

        stringBuilder.append("]");

        objectNode.put("message", stringBuilder.toString());

    }

}
