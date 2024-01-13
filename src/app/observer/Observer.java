package app.observer;

public interface Observer {
    /**
     * Send notification to all Observable objects followed/notifyAll.
     * @param message the message sent
     */
    void sendNotifications(String message);
}
