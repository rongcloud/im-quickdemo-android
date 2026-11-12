package cn.rongcloud.um.ui.api;

/** Bounded in-memory tail for listener output; no disk writes. Caller provides synchronization. */
final class BoundedLog {
    static final int LIMIT = 64 * 1024;
    static final String TRUNCATED = "[内容较多，仅显示最近 64K 字符]\n";
    private final StringBuilder text = new StringBuilder();
    private boolean truncated;

    void clear() { text.setLength(0); truncated = false; }
    void append(String entry) {
        if (entry.length() >= LIMIT) {
            text.setLength(0);
            text.append(entry, entry.length() - LIMIT, entry.length());
            truncated = true;
        } else {
            int excess = text.length() + entry.length() - LIMIT;
            if (excess > 0) { text.delete(0, excess); truncated = true; }
            text.append(entry);
        }
        if (text.length() > 0 && Character.isLowSurrogate(text.charAt(0))) text.deleteCharAt(0);
    }
    @Override public String toString() { return (truncated ? TRUNCATED : "") + text; }
}
