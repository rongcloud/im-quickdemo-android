package cn.rongcloud.um.ui.api;

import io.rong.imlib.ChannelClient;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum.CoreErrorCode;
import io.rong.imlib.RongCoreClient;

/** Request-scoped SDK adapter, shared callbacks and cleanup; never holds a page reference. */
final class ApiContext implements AutoCloseable {
    interface Success<T> { void accept(T value); }
    final ApiRequest request;
    final ApiLog log;
    final RongCoreClient core;
    final ChannelClient channel;

    ApiContext(ApiRequest request, ApiLog log) {
        this(request, log, RongCoreClient.getInstance(), ChannelClient.getInstance());
    }

    ApiContext(ApiRequest request, ApiLog log, RongCoreClient core, ChannelClient channel) {
        this.request = request;
        this.log = new ApiLog() {
            @Override public boolean isActive() { return !closed && log.isActive(); }
            @Override public void write(String text) { if (isActive()) log.write(text); }
        };
        this.core = core;
        this.channel = channel;
    }

    private volatile boolean closed;
    private final java.util.List<Runnable> cleanup = new java.util.ArrayList<>();
    private ConnectionDemo connection;
    private java.util.concurrent.ExecutorService worker;

    void own(Runnable close) {
        if (closed) close.run();
        else cleanup.add(close);
    }

    ConnectionDemo connection() {
        if (connection == null) {
            connection = new ConnectionDemo(log);
            own(connection::close);
        }
        return connection;
    }

    void background(Runnable task) {
        if (!log.isActive()) return;
        if (worker == null) {
            worker = java.util.concurrent.Executors.newSingleThreadExecutor();
            own(() -> worker.shutdownNow());
        }
        worker.execute(() -> {
            if (!log.isActive()) return;
            try { task.run(); }
            catch (RuntimeException e) { localError(e); }
        });
    }

    @Override public void close() {
        if (closed) return;
        closed = true;
        RuntimeException failure = null;
        for (int i = cleanup.size() - 1; i >= 0; i--) {
            try { cleanup.get(i).run(); }
            catch (RuntimeException e) {
                if (failure == null) failure = e;
                else failure.addSuppressed(e);
            }
        }
        cleanup.clear();
        if (failure != null) throw failure;
    }

    void loadMessage(Success<io.rong.imlib.model.Message> next) {
        log.write("前置调用 getMessage，messageId=" + request.integer(ApiCatalog.Param.MESSAGE_ID));
        core.getMessage(request.integer(ApiCatalog.Param.MESSAGE_ID), stage("getMessage", message -> {
            if (message == null) throw new IllegalArgumentException("找不到指定 messageId 对应的本地消息");
            next.accept(message);
        }));
    }

    <T> IRongCoreCallback.ResultCallback<T> callback() {
        return new IRongCoreCallback.ResultCallback<T>() {
            @Override public void onSuccess(T value) { success(value); }
            @Override public void onError(CoreErrorCode e) { error(e, null); }
        };
    }

    <T> IRongCoreCallback.ResultCallback<T> stage(String name, Success<T> next) {
        return new IRongCoreCallback.ResultCallback<T>() {
            @Override public void onSuccess(T value) {
                if (!log.isActive()) return;
                log.write(name + " 回调 onSuccess，错误码: 0\n结果:\n" + ApiLogFormatter.format(value));
                try { next.accept(value); }
                catch (RuntimeException e) { localError(e); }
            }
            @Override public void onError(CoreErrorCode e) { error(e, null); }
        };
    }

    IRongCoreCallback.OperationCallback operation() {
        return new IRongCoreCallback.OperationCallback() {
            @Override public void onSuccess() { success("OperationCallback.onSuccess（无返回值）"); }
            @Override public void onError(CoreErrorCode e) { error(e, null); }
        };
    }

    void success(Object value) {
        log.write("状态: 回调成功\n回调: onSuccess\n错误码: 0\n结果:\n" + ApiLogFormatter.format(value));
    }

    void error(CoreErrorCode e, Object value) {
        log.write("状态: SDK 调用失败\n回调: onError\n错误码: " + (e == null ? "null" : e.getValue())
                + "\n错误名称: " + e + "\n错误描述: " + (e == null ? "未知" : e.getMessage())
                + "\n结果:\n" + ApiLogFormatter.format(value));
    }

    void localError(RuntimeException e) {
        log.write("状态: 失败\n错误码: 无（本地参数校验或调用异常）\n错误描述: " + e.getClass().getSimpleName() + ": " + e.getMessage());
    }
}
