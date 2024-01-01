package app.pages;

import app.audio.Podcast;
import com.fasterxml.jackson.databind.node.ObjectNode;
import app.users.GeneralUser;
import app.users.Host;
import app.users.host_stuff.Announcement;

import java.util.ArrayList;

public final class HostPage implements Page {
    private Host owner;

    @Override
    public void printPage(final ObjectNode objectNode) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Podcasts:\n\t[");
        String separator = ", ";
        ArrayList<Podcast> podcasts = owner.getPodcasts();
        int size = podcasts.size();
        for (int i = 0; i < size; i++) {
            stringBuilder.append(podcasts.get(i).toString());
            if (i != size - 1) {
                stringBuilder.append(separator);
            }
        }

        stringBuilder.append("]\n\nAnnouncements:\n\t[");
        ArrayList<Announcement> announcements = owner.getAnnouncements();
        size = announcements.size();
        for (int i = 0; i < size; i++) {
            stringBuilder.append(announcements.get(i).toString());
            if (i != size - 1) {
                stringBuilder.append(separator);
            }
        }
        stringBuilder.append("]");

        objectNode.put("message", stringBuilder.toString());

    }

    @Override
    public void setOwner(final GeneralUser owner) {
        this.owner = (Host) owner;
    }

}
