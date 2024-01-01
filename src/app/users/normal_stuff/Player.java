package app.users.normal_stuff;


import app.commands.Command;
import lombok.Getter;
import app.utils.MyConst;
import app.users.User;
import app.audio.AudioCollection;
import app.audio.AudioFile;
import app.audio.AudioObject;
import app.audio.Song;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Collections;
import java.util.Random;

public class Player {
    @Getter
    private AudioObject source = null;
    @Getter
    private MyConst.SourceType sourceType;
    private User user;
    @Getter
    private AudioFile track = null;
    private ArrayList<AudioFile> trackList;
    private int size;
    private int trackId = 0;
    private int trackSeek = 0;
    private int trackDuration = 0;
    private boolean paused;
    private boolean frozen;
    private int startTime = 0;
    private int playerSeek = 0;
    private int duration;
    private final HashMap<AudioObject, Integer> lastRunTimeMap = new HashMap<>();
    private String name;
    private int repeat = 0;
    private static final String[] REPEAT_STATES;

    static {
        REPEAT_STATES = new String[]{"No Repeat", "Repeat All",
                "Repeat Current Song", "No Repeat", "Repeat Once", "Repeat Infinite"};
    }

    private boolean shuffle;
    private List<Integer> shuffleIndexes;

    /**
     * Check if player is running
     * this command updates the player to current timestamp
     * @param timestamp time when we check
     * @return true if player is playing at the moment
     */
    public boolean isPlaying(final int timestamp) {
        updateTrack(timestamp);
        return source != null;

    }

    /**
     * Determine the track that should play at a moment and update the player.
     * @param timestamp current time
     */
    private void updateTrack(final int timestamp) {
        if (paused) {
            return;
        }
        if (source == null) {
            return;
        }
        if (track == null && trackId != -1 && !trackList.isEmpty()) {
            track = trackList.get(trackId);
        } else if (track == null) {
            return;
        }
        trackDuration = track.getDuration();
        int nextId = generateNextId(trackId);
        int givenTime = timestamp - startTime;
        startTime = timestamp;
        //if player repeat state is No_Repeat
        if (repeat % MyConst.REPEAT_SIZE == MyConst.NO_REPEAT) {
            if (givenTime < trackDuration - trackSeek) {
                trackSeek += givenTime;

            } else {
                givenTime -= (trackDuration - trackSeek);
                while (nextId != -1) {
                    int nextDuration = trackList.get(nextId).getDuration();
                    givenTime = givenTime - nextDuration;
                    trackId = nextId;
                    track = trackList.get(trackId);
                    if (givenTime < 0) {
                        break;
                    }
                    nextId = generateNextId(trackId);
                }
                trackDuration = track.getDuration();
                if (givenTime > 0) {
                    ejectNoTime();
                } else if (givenTime < 0) {
                    trackSeek = trackDuration + givenTime;
                } else {
                    ejectNoTime();
                }

            }
        } else if (repeat == MyConst.REPEAT_CURRENT) {

            if (givenTime < trackDuration - trackSeek) {
                trackSeek += givenTime;

            } else {
                givenTime -= (trackDuration - trackSeek);
                while (givenTime > trackDuration) {
                    givenTime -= trackDuration;
                }
                trackSeek = givenTime;

            }
        } else if (repeat == MyConst.REPEAT_ALL || repeat == MyConst.REPEAT_INFINITE) {
            playerSeek = getPlayerSeek();
            playerSeek = playerSeek + givenTime;
            if (playerSeek >= duration) {
                playerSeek = playerSeek % duration;
            }
            setTrackSeek();

        } else if (repeat == MyConst.REPEAT_ONCE) {
            playerSeek = getPlayerSeek();
            playerSeek = playerSeek + givenTime;
            if (playerSeek >= duration && playerSeek < 2 * duration) {
                repeat = MyConst.NO_REPEAT;
                playerSeek = playerSeek - duration;
                setTrackSeek();
            } else if (playerSeek >= 2 * duration) {
                ejectNoTime();
            } else {
                trackSeek += givenTime;
            }
        }


    }

    private Integer getPlayerSeek() {
        int i;
        if (shuffleIndexes != null) { //case of Song
            i = shuffleIndexes.get(0);
        } else {
            i = 0;
        }
        int sum = 0;
        while (i != trackId && i != -1) {
            sum += trackList.get(i).getDuration();
            i = generateNextId(i);
        }
        return sum + trackSeek;
    }

    private void setTrackSeek() {
        if (sourceType == MyConst.SourceType.SONG) {
            trackSeek = playerSeek;

        } else {
            int i = 0;
            int sum = 0;
            if (playerSeek == 0) {
                trackId = shuffleIndexes.get(0);
                track = trackList.get(trackId);
                trackSeek = 0;
            } else {
                while (sum <= playerSeek) {
                    track = trackList.get(shuffleIndexes.get(i));
                    trackId = shuffleIndexes.get(i);
                    trackDuration = track.getDuration();
                    sum += trackList.get(shuffleIndexes.get(i)).getDuration();
                    i++;
                }

                sum = sum - trackDuration;
                trackSeek = playerSeek - sum;
            }
            trackDuration = track.getDuration();
        }
    }

    /**
     * Load a source in the player. To succeed you need to use select first.
     *
     * @param cmd       used to get timestamp and set last command
     * @param objectNode for writing to output
     */
    public void load(final Command cmd, final ObjectNode objectNode) {
        if (!user.isConnected()) {
            user.standardOfflineCommand("load", objectNode);
            return;
        }
        String lastCommand = user.getLastCommand();
        AudioObject selectedObject = user.getSearchBar().getSelectedAudio();
        if (!lastCommand.equals("select")) {
            objectNode.put("message", "Please select a source before attempting to load.");
        } else if (selectedObject == null) {
            objectNode.put("message", "You can't load an empty audio collection!");
        } else {
            objectNode.put("message", "Playback loaded successfully.");
            source = selectedObject;
            sourceType = selectedObject.getType();
            if (sourceType == MyConst.SourceType.PLAYLIST
                    || sourceType == MyConst.SourceType.PODCAST
                    || sourceType == MyConst.SourceType.ALBUM) {
                AudioCollection collectionSource = (AudioCollection) source;
                trackList = collectionSource.getTracks();
                size = trackList.size();
                shuffleIndexes = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    shuffleIndexes.add(i);
                }

            } else if (sourceType == MyConst.SourceType.SONG) {
                track = (AudioFile) source;
                trackList = null;
                size = 1;
            }
            trackId = 0;
            startTime = cmd.getTimestamp();
            if (sourceType == MyConst.SourceType.PODCAST) {
                playerSeek = lastRunTimeMap.getOrDefault(source, 0);
                if (playerSeek != 0) {
                    setTrackSeek();
                }
            }
            duration = source.getDuration();
            paused = false;
        }
        user.setLastCommand(cmd.getCommand());
    }

    private void ejectNoTime() {
        duration = 0;
        name = "";
        source = null;
        paused = true;
        track = null;
        trackDuration = 0;
        trackSeek = 0;
        shuffle = false;
        repeat = 0;

    }

    /**
     * Unloads the source from the player. Used after Search.
     *
     * @param timestamp for updating track status before unloading
     */
    public void unload(final int timestamp) {
        if (source == null) {
            return;
        }
        repeat = MyConst.NO_REPEAT;
        updateTrack(timestamp);
        if (source != null) {
            if (sourceType == MyConst.SourceType.PODCAST) {
                int runningTime = getPlayerSeek();
                lastRunTimeMap.put(source, runningTime);
            }
        }
        ejectNoTime();
    }

    /**
     * Play or pause the player.
     *
     * @param timestamp  for timestamp
     * @param objectNode for output
     */
    public void playPause(final int timestamp, final ObjectNode objectNode) {
        if (!user.isConnected()) {
            user.standardOfflineCommand("playPause", objectNode);
            return;
        }
        if (!isPlaying(timestamp)) {
            objectNode.put("message", "Please load a source before attempting to"
                    + " pause or resume playback.");
        } else {
            if (paused) {
                objectNode.put("message", "Playback resumed successfully.");
                paused = false;
                startTime = timestamp;

            } else {
                objectNode.put("message", "Playback paused successfully.");
                updateTrack(timestamp);
                paused = true;
            }
        }
    }

    /**
     * Freeze the player. Used when user is set to be offline.
     * @param timestamp for updating the track
     */
    public void freeze(final int timestamp) {
        frozen = paused;
        updateTrack(timestamp);
        paused = true;
    }

    /**
     * Unfreeze the player. Used when user is set to be online again.
     * @param timestamp for updating the track.
     */
    public void unfreeze(final int timestamp) {
        if (source == null) {
            return;
        }
        startTime = timestamp;
        paused = frozen;
        updateTrack(timestamp);
        // paused = true;
    }

    /**
     * Change repeat state.
     *
     * @param cmd        for timestamp
     * @param objectNode for output
     */
    public void repeat(final Command cmd, final ObjectNode objectNode) {
        if (!user.isConnected()) {
            user.standardOfflineCommand("repeat", objectNode);
            return;
        }
        if (!isPlaying(cmd.getTimestamp())) {
            objectNode.put("message", "Please load a source before setting the repeat status.");
        } else {
            repeat++;
            repeat = repeat % MyConst.REPEAT_SIZE;
            //internal convention of repeat index
            if (sourceType == MyConst.SourceType.PODCAST
                    || sourceType == MyConst.SourceType.SONG) {
                repeat += MyConst.REPEAT_SIZE;
            }
            objectNode.put("message", "Repeat mode changed to "
                    + REPEAT_STATES[repeat].toLowerCase() + ".");
        }
    }

    /**
     * Turns on or off the shuffle.
     *
     * @param cmd        for getting the random seed
     * @param objectNode for output
     */
    public void shuffle(final Command cmd, final ObjectNode objectNode) {
        if (!user.isConnected()) {
            user.standardOfflineCommand("shuffle", objectNode);
            return;
        }
        updateTrack(cmd.getTimestamp());
        if (source == null) {
            objectNode.put("message", "Please load a source"
                    + " before using the shuffle function.");
        } else if (sourceType != MyConst.SourceType.PLAYLIST
                && sourceType != MyConst.SourceType.ALBUM) {
            objectNode.put("message", "The loaded source is not a playlist or an album.");
        } else if (shuffle) {
            shuffle = false;
            shuffleIndexes = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                shuffleIndexes.add(i);
            }
            objectNode.put("message", "Shuffle function deactivated successfully.");
        } else {
            shuffle = true;
            shuffleIndexes = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                shuffleIndexes.add(i);
            }
            Collections.shuffle(shuffleIndexes, new Random(cmd.getSeed()));
            objectNode.put("message", "Shuffle function activated successfully.");
        }
    }

    /**
     * Jump to next track from list.
     *
     * @param cmd        for timestamp
     * @param objectNode for output
     */
    public void next(final Command cmd, final ObjectNode objectNode) {
        if (!user.isConnected()) {
            user.standardOfflineCommand("next", objectNode);
            return;
        }
        updateTrack(cmd.getTimestamp());
        int fakeTimestamp;
        fakeTimestamp = startTime + (trackDuration - trackSeek);
        //if player was paused next make it run again
        paused = false;
        updateTrack(fakeTimestamp);
        startTime = cmd.getTimestamp();
        trackSeek = 0;
        if (source == null) {
            ejectNoTime();
            objectNode.put("message", "Please load a source before skipping to the next track.");
        } else {
            objectNode.put("message", "Skipped to next track successfully."
                    + " The current track is " + track.getName() + ".");
        }
    }

    /**
     * Jump to previous track from list.
     *
     * @param cmd        for timestamp
     * @param objectNode for output
     */
    public void prev(final Command cmd, final ObjectNode objectNode) {
        if (!user.isConnected()) {
            user.standardOfflineCommand("prev", objectNode);
            return;
        }
        updateTrack(cmd.getTimestamp());
        boolean success = source != null;
        if (trackSeek >= 1) {
            trackSeek = 0;
            startTime = cmd.getTimestamp();

        } else if (trackSeek == 0) {
            if (generatePrevId(trackId) != -1) {
                trackId = generatePrevId(trackId);
                track = trackList.get(trackId);
            }
            trackSeek = 0;
            startTime = cmd.getTimestamp();
        }
        if (success) {
            objectNode.put("message", "Returned to previous track successfully."
                    + " The current track is "
                    + track.getName() + ".");
            trackDuration = track.getDuration();
            paused = false;
        } else {
            ejectNoTime();
            objectNode.put("message", "Please load a source before returning"
                    + " to the previous track.");
        }
    }

    /**
     * Seek 90sec forward.
     *
     * @param cmd        for timestamp
     * @param objectNode for output
     */
    public void forward(final Command cmd, final ObjectNode objectNode) {
        if (!user.isConnected()) {
            user.standardOfflineCommand("forward", objectNode);
            return;
        }
        if (!isPlaying(cmd.getTimestamp())) {
            objectNode.put("message", "Please load a source before attempting to forward.");
        } else if (sourceType == MyConst.SourceType.PODCAST) {
            int remainingTrack = trackDuration - trackSeek;
            if (remainingTrack > MyConst.SKIP_TIME) {
                trackSeek += MyConst.SKIP_TIME;
                objectNode.put("message", "Skipped forward successfully.");
            } else {
                int fakeTimestamp = startTime + remainingTrack;
                updateTrack(fakeTimestamp);
                objectNode.put("message", "Skipped forward successfully.");
            }
        } else {
            objectNode.put("message", "The loaded source is not a podcast.");
        }
    }

    /**
     * Rewind 90sec.
     *
     * @param cmd        for timestamp
     * @param objectNode for output
     */
    public void backward(final Command cmd, final ObjectNode objectNode) {
        if (!user.isConnected()) {
            user.standardOfflineCommand("backward", objectNode);
            return;
        }
        if (!isPlaying(cmd.getTimestamp())) {
            objectNode.put("message", "Please select a source before rewinding.");
        } else if (sourceType == MyConst.SourceType.PODCAST) {
            if (trackSeek < MyConst.SKIP_TIME) {
                trackSeek = 0;
            } else {
                trackSeek -= MyConst.SKIP_TIME;
            }
            objectNode.put("message", "Rewound successfully.");
        } else {
            objectNode.put("message", "The loaded source is not a podcast.");
        }
    }

    /**
     * Show player status at a moment.
     *
     * @param cmd          for timestamp
     * @param objectNode   for output
     */
    public void status(final Command cmd, final ObjectNode objectNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode statsNode = objectMapper.createObjectNode();
        updateTrack(cmd.getTimestamp());
        if (track != null) {
            name = track.getName();
        }
        statsNode.put("name", name);
        statsNode.put("remainedTime", trackDuration - trackSeek);
        statsNode.put("repeat", REPEAT_STATES[repeat]);
        statsNode.put("shuffle", shuffle);
        if (user.isConnected()) {
            statsNode.put("paused", paused);
        } else {
            if (source != null) {
                statsNode.put("paused", frozen);
            } else {
                statsNode.put("paused", true);
            }
        }
        objectNode.set("stats", statsNode);
    }

    public Player() {
    }

    private Integer generateNextId(final int id) {
        if (shuffle) {
            int i = shuffleIndexes.indexOf(id);
            if (i + 1 < size) {
                return shuffleIndexes.get(i + 1);
            }
        } else {
            if (id + 1 < size) {
                return id + 1;
            }
        }
        return -1;
    }

    private Integer generatePrevId(final int id) {
        if (shuffle) {
            int i = shuffleIndexes.indexOf(id);
            if (i - 1 >= 0) {
                return shuffleIndexes.get(i - 1);
            }
        } else {
            if (id - 1 >= 0) {
                return id - 1;
            }
        }
        return -1;
    }

    /**
     * If a song is playing, get the song downcast. Used by user to like the current song.
     *
     * @param timestamp time
     * @return Song instance of current playing track
     */
    public Song getSongTrack(final int timestamp) {
        updateTrack(timestamp);
        if (track == null) {
            return null;
        }
        if (track.getType() == MyConst.SourceType.SONG) {
            return (Song) track;
        }
        return null;
    }

    /**
     * Set the player's user (owner).
     * @param user the user
     */
    public void setUser(final User user) {
        this.user = user;
    }
}

