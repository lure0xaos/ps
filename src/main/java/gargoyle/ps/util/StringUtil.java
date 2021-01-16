package gargoyle.ps.util;

public final class StringUtil {

    public static String join(String delimiter, Object... objects) {
        StringBuilder s = new StringBuilder();
        for (Object o : objects) {
            if (o != null && (!(o instanceof String && ((String) o).trim().isEmpty()))) {
                if (s.length() != 0) { s.append(delimiter); }
                s.append(o);
            }
        }
        return s.toString();
    }

    public static String optional(boolean condition, String value) {
        return condition ? value : "";
    }
}
