package app.audio;

import app.analytics.monetization.ArtistIncome;
import lombok.Getter;
import app.users.Artist;
import app.users.GeneralUser;
import app.users.Host;
import app.utils.MyConst;
import app.users.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.LibraryInput;
import fileio.input.PodcastInput;
import fileio.input.SongInput;
import fileio.input.UserInput;

import java.util.ArrayList;
import java.util.TreeSet;

@Getter
public final class Library {
    private ArrayList<Song> songs = new ArrayList<>();
    private final ArrayList<Podcast> podcasts = new ArrayList<>();
    private ArrayList<User> users = new ArrayList<>();
    private final ArrayList<Artist> artists = new ArrayList<>();
    private final ArrayList<Host> hosts = new ArrayList<>();


    //copy Library from LibraryInput
    public Library(final LibraryInput libraryInput) {
        for (SongInput songInput : libraryInput.getSongs()) {
            Song song = new Song(songInput);
            songs.add(song);
        }
        for (PodcastInput podcastInput : libraryInput.getPodcasts()) {
            Podcast podcast = new Podcast(podcastInput);
            podcasts.add(podcast);
        }
        //move data from UserInput to User
        for (UserInput userInput : libraryInput.getUsers()) {
            User user = new User(userInput.getUsername(), userInput.getCity(), userInput.getAge());
            users.add(user);
        }

    }

    /**
     * Get top 5 playlists by number of followers.
     * If there are two with same number of followers, they are sorted ascending by creationTime.
     * @param objectNode   for parsing json
     */
    public void getTop5Playlists(final ObjectNode objectNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayList<Playlist> sortedPlaylists = new ArrayList<>();
        for (User user : users) {
            for (Playlist playlist : user.getPlaylists()) {
                if (playlist.isVisible()) {
                    sortedPlaylists.add(playlist);
                }
            }
        }
        sortedPlaylists.sort((a, b) -> {
            int followersComparison = Integer.compare(b.getFollowers(), a.getFollowers());
            if (followersComparison != 0) {
                return followersComparison;
            } else {
                return Integer.compare(a.getCreationTime(), b.getCreationTime());
            }
        });
        int minSize = Integer.min(MyConst.RESULT_SIZE, sortedPlaylists.size());
        ArrayNode playNode = objectMapper.createArrayNode();
        for (int i = 0; i < minSize; i++) {
            playNode.add(sortedPlaylists.get(i).getName());
        }
        objectNode.set("result", playNode);
    }

    /**
     * Get top 5 songs by number of likes.
     * @param objectNode   for output containing list of names
     */
    public void getTop5Songs(final ObjectNode objectNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayList<Song> sortedSongs = new ArrayList<>(songs);
        sortedSongs.sort((a, b) -> Integer.compare(b.getLikes(), a.getLikes()));
        sortedSongs.subList(MyConst.RESULT_SIZE, sortedSongs.size()).clear();
        ArrayNode songNode = objectMapper.createArrayNode();
        for (Song s : sortedSongs) {
            songNode.add(s.getName());
        }
        objectNode.set("result", songNode);
    }

    /**
     * Get top 5 albums by total likes.
     * If there are two albums with same number of likes, they're sorted alphabetically.
     * @param objectNode for output containing list of names
     */
    public void getTop5Albums(final ObjectNode objectNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayList<Album> topAlbums = new ArrayList<>();
        for (Artist artist : artists) {
            topAlbums.addAll(artist.getAlbums());
        }
        topAlbums.sort((a, b) -> {
            int likesComparison = Integer.compare(b.calculateLikes(), a.calculateLikes());
            if (likesComparison == 0) {
                return a.getName().compareTo(b.getName());
            } else {
                return likesComparison;
            }
        });
        int size = Integer.min(MyConst.RESULT_SIZE, topAlbums.size());

        ArrayNode albumsNode = objectMapper.createArrayNode();
        for (int i = 0; i < size; i++) {
            albumsNode.add(topAlbums.get(i).getName());
        }
        objectNode.set("result", albumsNode);

    }

    /**
     * Get top 5 artists by total likes.
     * @param objectNode for output containing list of usernames
     */
    public void getTop5Artists(final ObjectNode objectNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayList<Artist> topArtists = new ArrayList<>(artists);
        topArtists.sort((a, b) -> Integer.compare(b.calculateLikes(), a.calculateLikes()));
        int size = Integer.min(MyConst.RESULT_SIZE, topArtists.size());
        ArrayNode artistsNode = objectMapper.createArrayNode();
        for (int i = 0; i < size; i++) {
            artistsNode.add(topArtists.get(i).getUsername());
        }
        objectNode.set("result", artistsNode);
    }

    /**
     * Get the normal users online.
     * @param objectNode for output containing list of usernames
     */
    public void getOnlineUsers(final ObjectNode objectNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode onlineUsers = objectMapper.createArrayNode();
        for (User user : users) {
            if (user.isConnected()) {
                onlineUsers.add(user.getUsername());
            }
        }
        objectNode.set("result", onlineUsers);
    }

    /**
     * Get a list of all users having any type.
     * @param objectNode for output containing list of usernames
     */
    public void getAllUsers(final ObjectNode objectNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode allUsers = objectMapper.createArrayNode();
        for (User user : users) {
            allUsers.add(user.getUsername());
        }
        for (Artist artist : artists) {
            allUsers.add(artist.getUsername());
        }
        for (Host host : hosts) {
            allUsers.add(host.getUsername());
        }
        objectNode.set("result", allUsers);
    }

    public ObjectNode endProgram() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        ObjectNode objectNode1 = objectMapper.createObjectNode();
        TreeSet<ArtistIncome> artistIncomes = new TreeSet<>();
        for (Artist artist : artists) {
            if(artist.getStats().hasFans() || artist.getIncome().getMerchRevenue() > 0.0) {
                artistIncomes.add(artist.getIncome());
            }
        }
        int ranking = 1;
        for (ArtistIncome artistIncome : artistIncomes) {
            artistIncome.setRanking(ranking++);
            objectNode1.set(artistIncome.getArtist().getUsername(), artistIncome.toObjectNode());
        }
        objectNode.put("command", "endProgram");
        objectNode.set("result", objectNode1);
        return objectNode;
    }

    /**
     * Check if there is a user of any type having the given name.
     * @param name the username of searched user
     * @return true if any user with given name exists
     */
    public boolean generalUserExists(final String name) {
        GeneralUser genUser = getGeneralUser(name);
        return genUser != null;
    }

    /**
     * Get a general user instance by name.
     * @param name username of searched user
     * @return an object representing searched user or null
     */
    public GeneralUser getGeneralUser(final String name) {
        for (User user : users) {
            if (user.getUsername().equals(name)) {
                return user;
            }
        }
        for (Artist artist : artists) {
            if (artist.getUsername().equals(name)) {
                return artist;
            }
        }
        for (Host host : hosts) {
            if (host.getUsername().equals(name)) {
                return host;
            }
        }
        return null;
    }
    public GeneralUser getUserOfType(final String name, final MyConst.UserType type) {
        switch (type) {
            case USER -> {
                for (User user : users) {
                    if (user.getUsername().equals(name)) {
                        return user;
                    }
                }
            }
            case ARTIST -> {
                for (Artist artist : artists) {
                    if (artist.getUsername().equals(name)) {
                        return artist;
                    }
                }
            }
            case HOST -> {
                for (Host host : hosts) {
                    if (host.getUsername().equals(name)) {
                        return host;
                    }
                }
            }
        }
        return null;
    }
    /**
     * Add a user to users list.
     * @param user user to be added
     */
    public void addUser(final User user) {
        users.add(user);
    }

    /**
     * Add an artist to artists list.
     * @param artist artist to be added
     */
    public void addArtist(final Artist artist) {
        artists.add(artist);
    }

    /**
     * Add a host to hosts list.
     * @param host host to be added
     */
    public void addHost(final Host host) {
        hosts.add(host);
    }




}
