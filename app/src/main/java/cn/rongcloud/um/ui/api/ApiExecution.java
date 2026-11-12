package cn.rongcloud.um.ui.api;

public interface ApiExecution extends AutoCloseable {
    void execute();
    void connectForDemo(String token);
    void disconnectForDemo();
    @Override void close();
}
