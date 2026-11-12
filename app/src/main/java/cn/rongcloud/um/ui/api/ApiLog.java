package cn.rongcloud.um.ui.api;

/** A request-scoped output port. SDK features do not reference an Activity. */
public interface ApiLog {
    void write(String text);
    boolean isActive();
}
