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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Random;
import java.util.LinkedHashMap;

public class Player {
    @Getter
    private AudioObject source = null;
    @Getter
    private MyConst.SourceType sourceType;
    private final User user;
    @Getter
    private AudioFile track = null;
    private ArrayList<AudioFile> trackList;
    private int trackId = 0;
    @Getter
    private int trackSeek = 0;
    private int trackDuration = 0;
    private boolean paused = true;
    private boolean frozen;
    private int startTime = 0;
    private int playerSeek = 0;
    private int duration;
    private final HashMap<AudioObject, Integer> lastRunTimeMap = new HashMap<>();
    private String name = "";
    private int repeat = 0;
    private static final String[] REPEAT_STATES;

    static {
        REPEAT_STATES = new String[]{"No Repeat", "Repeat All",
                "Repeat Current Song", "No Repeat", "Repeat Once", "Repeat Infinite"};
    }

    private boolean shuffle;
    private LinkedList<Integer> shuffleIndexes;

    /**
     * Check if player is running
     * this command updates the player to current timestamp
     *
     * @param timestamp time when we check
     * @return true if player is playing at the moment
     */
    public boolean isPlaying(final int timestamp) {
        updateTrack(timestamp);
        return source != null;

    }

    /**
     * Determine the track that should play at a moment and update the player.
     *
     * @param timestamp current time
     */
    private void updateTrack(final int timestamp) {
        if (paused) {
            return;
        }
        if (source == null) {
            return;
        }
        if (track == null && !trackList.isEmpty()) {
            track = trackList.get(trackId);
            user.getStats().updateStats(track, source);
        } else if (track == null) {
            return;
        }
        trackDuration = track.getDuration();
        int givenTime = timestamp - startTime;
        startTime = timestamp;
        //if player repeat state is No_Repeat
        if (repeat % MyConst.REPEAT_SIZE == MyConst.NO_REPEAT) {
            if (givenTime < trackDuration - trackSeek) {
                trackSeek += givenTime;

            } else {
                givenTime -= (trackDuration - trackSeek);
                int nextId = generateNextId(trackId, true);
                while (nextId != -1) {
                    int nextDuration = trackList.get(nextId).getDuration();
                    givenTime = givenTime - nextDuration;
                    trackId = nextId;
                    track = trackList.get(trackId);
                    if (givenTime < 0) {
                        break;
                    }
                    nextId = generateNextId(trackId, true);
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
                // remove ads from previous play that were put in trackList and update
                removeAds(playerSeek / duration);
                playerSeek = playerSeek % duration;
            }
            setTrackSeek(true);

        } else if (repeat == MyConst.REPEAT_ONCE) {
            playerSeek = getPlayerSeek();
            playerSeek = playerSeek + givenTime;
            if (playerSeek >= duration && playerSeek < 2 * duration) {
                repeat = MyConst.NO_REPEAT;
                playerSeek = playerSeek - duration;
                setTrackSeek(true);
            } else if (playerSeek >= 2 * duration) {
                ejectNoTime();
            } else {
                trackSeek += givenTime;
            }
        }
    }

    private Integer getPlayerSeek() {
        int i = shuffleIndexes.get(0);
        int sum = 0;
        while (i != trackId && i != -1) {
            sum += trackList.get(i).getDuration();
            i = generateNextId(i, false);
        }
        return sum + trackSeek;
    }

    private void setTrackSeek(final boolean update) {
        int i = 0;
        int sum = 0;
        if (playerSeek == 0) {
            trackId = shuffleIndexes.get(0);
            track = trackList.get(trackId);
            trackSeek = 0;
        } else {
            while (sum <= playerSeek) {
                track = trackList.get(shuffleIndexes.get(i));
                if (update) {
                    user.getStats().updateStats(track, source);
                }
                trackId = shuffleIndexes.get(i);
                trackDuration = track.getDuration();
                sum += trackList.get(shuffleIndexes.get(i)).getDuration();
                i++;
            }

            sum = sum - trackDuration;
            trackSeek = playerSeek - sum;
        }
        trackDuration = track.getDuration();
        user.getStats().updateStats(track, source);
    }

    /**
     * Load a source into player. No output is provided.
     * @param selectedObject the source
     * @param timestamp for updating the player
     */
    public void loadSource(final AudioObject selectedObject, final int timestamp) {
        unload(timestamp);
        source = selectedObject;
        sourceType = selectedObject.getType();
        if (sourceType == MyConst.SourceType.PLAYLIST
                || sourceType == MyConst.SourceType.PODCAST
                || sourceType == MyConst.SourceType.ALBUM) {
            AudioCollection collectionSource = (AudioCollection) source;
            trackList = collectionSource.getTracks();
            shuffleIndexes = new LinkedList<>();
            for (int i = 0; i < trackList.size(); i++) {
                shuffleIndexes.add(i);
            }

        } else if (sourceType == MyConst.SourceType.SONG) {
            // song is considered a playlist with just one track
            trackList = new ArrayList<>();
            trackList.add((AudioFile) source);
            shuffleIndexes = new LinkedList<>();
            shuffleIndexes.add(0);

        }
        startTime = timestamp;
        //load podcast with time left from last play
        if (sourceType == MyConst.SourceType.PODCAST) {
            playerSeek = lastRunTimeMap.getOrDefault(source, 0);
            if (playerSeek != 0) {
                setTrackSeek(false);
            }
        }
        duration = source.getDuration();
        paused = false;
        updateTrack(timestamp);
    }

    /**
     * Load a source in the player. To succeed you need to use select first.
     *
     * @param cmd        used to get timestamp and set last command
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
            loadSource(selectedObject, cmd.getTimestamp());
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
        trackId = 0;
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
     *
     * @param timestamp for updating the track
     */
    public void freeze(final int timestamp) {
        frozen = paused;
        updateTrack(timestamp);
        paused = true;
    }

    /**
     * Unfreeze the player. Used when user is set to be online again.
     *
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
            shuffleIndexes = new LinkedList<>();
            for (int i = 0; i < trackList.size(); i++) {
                shuffleIndexes.add(i);
            }
            objectNode.put("message", "Shuffle function deactivated successfully.");
        } else {
            shuffle = true;
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
        } else if (track.isAd()) {
            objectNode.put("message", "You can't skip an ad.");
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
        if (success) {
            if (trackSeek >= 1) {
                trackSeek = 0;
                startTime = cmd.getTimestamp();

            } else if (trackSeek == 0) {
                if (generatePrevId(trackId, false) != -1) {
                    trackId = generatePrevId(trackId, true);
                    track = trackList.get(trackId);
                }
                trackSeek = 0;
                startTime = cmd.getTimestamp();
            }
            objectNode.put("message", "Returned to previous track successfully."
                    + " The current track is "
                    + track.getName() + ".");
            trackDuration = track.getDuration();
            paused = false;
        } else if (track.isAd()) {
            objectNode.put("message", "You can't skip an ad!");
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
     * @param cmd        for timestamp
     * @param objectNode for output
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

    public Player(final User user) {
        this.user = user;
    }

    private Integer generateNextId(final int id, final boolean update) {
        int nextId = -1;
        int i = shuffleIndexes.indexOf(id);
        if (i + 1 < trackList.size()) {
            nextId = shuffleIndexes.get(i + 1);
        }
        if (nextId != -1 && update) {
            user.getStats().updateStats(trackList.get(nextId), source);
        }
        return nextId;
    }

    private Integer generatePrevId(final int id, final boolean update) {
        int prevId = -1;
        int i = shuffleIndexes.indexOf(id);
        if (i - 1 >= 0) {
            prevId = shuffleIndexes.get(i - 1);
        }
        if (prevId != -1 && update) {
            user.getStats().updateStats(trackList.get(prevId), source);
        }
        return prevId;
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

    private void insertAd(final Song ad) {
        int size = trackList.size();
        for (int i = 0; i < size; i++) {
            if (shuffleIndexes.get(i) > trackId) {
                shuffleIndexes.set(i, shuffleIndexes.get(i) + 1);
            }
        }
        trackList.add(trackId + 1, ad);
        shuffleIndexes.add(trackId + 1, trackId + 1);
    }

    private void removeAds(final int numberOfRepeats) {
        LinkedHashMap<AudioFile, Integer> adsHashMap = new LinkedHashMap<>();
        int size = trackList.size();
        for (int i = size - 1; i >= 0; i--) {
            if (trackList.get(i).isAd()) {
                adsHashMap.put(trackList.get(i), i);
            }
        }
        for (AudioFile ad : adsHashMap.keySet()) {
            trackList.remove(ad);
            int adIndex = adsHashMap.get(ad);
            for (int i = shuffleIndexes.size() - 1; i >= 0; i--) {
                if (shuffleIndexes.get(i) > adIndex) {
                    shuffleIndexes.set(i, shuffleIndexes.get(i) - 1);
                }
            }
            shuffleIndexes.remove(adIndex);
        }
        int rep = numberOfRepeats;
        while (rep > 0) {
            for (AudioFile track1 : trackList) {
                user.getStats().updateStats(track1, source);
            }
            rep--;
        }
    }

    /**
     * Insert an ad after current track.
     * @param timestamp to update the current track
     * @param objectNode for output
     * @param ad the ad
     */
    public void adBreak(final int timestamp, final ObjectNode objectNode, final Song ad) {
        if (!isPlaying(timestamp)) {
            objectNode.put("message", user.getUsername() + " is not playing any music.");
        } else {
            objectNode.put("message", "Ad inserted successfully.");
            insertAd(ad);
        }
    }

}

