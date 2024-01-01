package app.users.artist_stuff;



public record Merch(String name, String description, int price) {
    /**
     *
     * @return string in format "name - price:\n\t description"
     */
    @Override
    public String toString() {
        return name + " - " + price + ":\n\t" + description;
    }
}
