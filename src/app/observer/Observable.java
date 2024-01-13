package app.observer;

public interface Observable {
    /**
     * Receive a notification sent by an observer.
     * @param message the notifications message.
     */
    void receiveNotification(String message);
}
