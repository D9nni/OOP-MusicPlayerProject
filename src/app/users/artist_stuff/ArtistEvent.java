package app.users.artist_stuff;



public record ArtistEvent(String name, String description, String date) {
    /**
     *
     * @return string in format "name - date:\n\t description"
     */
    @Override
    public String toString() {
        return name + " - " + date + ":\n\t" + description;
    }
}
