package app.utils;

public class Functions {
    public static int truncSizeTo5(int size) {
        return Integer.min(size, MyConst.RESULT_SIZE);
    }
}
