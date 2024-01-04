package app.analytics.wrapped;

import app.users.Host;
import app.users.User;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;

public class HostStats implements Wrapped {
    private final HashMap<User, Integer> fans = new HashMap<>();
    private final Host host;

    public HostStats(Host host) {
        this.host = host;
    }

    @Override
    public void wrapped(ObjectNode objectNode) {

    }
}
