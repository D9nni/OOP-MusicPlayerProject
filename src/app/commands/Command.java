package app.commands;

import fileio.input.EpisodeInput;
import fileio.input.Filters;
import fileio.input.SongInput;
import lombok.Getter;

import java.util.ArrayList;

@Getter
public final class Command {
    private String command;
    private String username;
    private int timestamp;
    private String type;
    private Filters filters;
    private String artist;
    private int itemNumber;
    private int playlistId;
    private String playlistName;
    private int seed;
    private int age;
    private String city;
    private String name;
    private int releaseYear;
    private String description;
    private String date;
    private int price;
    private ArrayList<EpisodeInput> episodes;
    private ArrayList<SongInput> songs;
    private String nextPage;
    private String recommendationType;

    public void setNextPage(final String nextPage) {
        this.nextPage = nextPage;
    }

    public void setEpisodes(final ArrayList<EpisodeInput> episodes) {
        this.episodes = episodes;
    }

    public void setPrice(final int price) {
        this.price = price;
    }

    public void setDate(final String date) {
        this.date = date;
    }

    public void setSongs(final ArrayList<SongInput> songs) {
        this.songs = songs;
    }


    public void setDescription(final String description) {
        this.description = description;
    }

    public void setReleaseYear(final int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public void setAge(final int age) {
        this.age = age;
    }

    public void setPlaylistId(final int playlistId) {
        this.playlistId = playlistId;
    }

    public void setPlaylistName(final String playlistName) {
        this.playlistName = playlistName;
    }

    public void setSeed(final int seed) {
        this.seed = seed;
    }

    public Command() {
    }

    public void setCommand(final String command) {
        this.command = command;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setTimestamp(final int timestamp) {
        this.timestamp = timestamp;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setFilters(final Filters filters) {
        this.filters = filters;
    }

    public void setArtist(final String artist) {
        this.artist = artist;
    }

    public void setItemNumber(final int itemNumber) {
        this.itemNumber = itemNumber;
    }

    public void setRecommendationType(final String recommendationType) {
        this.recommendationType = recommendationType;
    }
}


