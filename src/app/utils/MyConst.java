package app.utils;

public final class MyConst {
    private MyConst() {
    }
    public static final Integer RESULT_SIZE = 5;
    public static final Integer NO_REPEAT = 0;
    public static final Integer REPEAT_ALL = 1;
    public static final Integer REPEAT_CURRENT = 2;
    public static final Integer REPEAT_ONCE = 4;
    public static final Integer REPEAT_INFINITE = 5;
    public static final Integer REPEAT_SIZE = 3;
    public static final Integer SKIP_TIME = 90;
    public static final Double USER_CREDIT = 1000000.0;
    public static final Double ROUND_VALUE = 100.0;

    public enum UserType {
        USER,
        HOST,
        ARTIST
    }
    public enum SourceType {
        SONG,
        PLAYLIST,
        PODCAST,
        EPISODE, // not used
        ALBUM,
    }

}
