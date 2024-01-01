package app.users.host_stuff;

public record Announcement(String name, String description) {
    /**
     *
     * @return string in format "name:\n\t description \n"
     */
    @Override
    public String toString() {
        return name + ":\n\t" + description + "\n";
    }
}
