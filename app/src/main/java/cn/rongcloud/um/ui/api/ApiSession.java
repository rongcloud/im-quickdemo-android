package cn.rongcloud.um.ui.api;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

/** Page request lifecycle. Public commands and the UI executor run on the UI thread. */
public final class ApiSession implements AutoCloseable {
    public interface View { void showLog(String text); }
    public interface Identity {
        String userId();
        String token();
    }
    interface Factory { ApiExecution create(ApiRequest request, ApiLog log); }

    private final ApiCatalog.Api api;
    private final Executor ui;
    private final Identity identity;
    private final Factory factory;
    private final BoundedLog text = new BoundedLog();
    private String header = "";
    private View view;
    private ApiExecution execution;
    private volatile long generation;
    private volatile long logRevision;
    private volatile boolean closed;

    public ApiSession(ApiCatalog.Api api, Executor ui, Identity identity, View view) {
        this(api, ui, identity, view, ApiRunner::new);
    }
    ApiSession(ApiCatalog.Api api, Executor ui, Identity identity, View view, Factory factory) {
        this.api = api;
        this.ui = ui;
        this.identity = identity;
        this.view = view;
        this.factory = factory;
    }

    public void execute(Map<String, String> input) {
        if (closed) return;
        release();
        reset("执行接口调用");
        try {
            ApiRequest request = new ApiRequest(api, input, identity.userId(), System.currentTimeMillis());
            ApiLog log = scopedLog();
            log.write("入参（包含默认值）:\n" + ApiLogFormatter.format(request.effective)
                    + "\n使用默认值的字段: " + ApiLogFormatter.format(request.defaulted));
            execution = factory.create(request, log);
            execution.execute();
        } catch (RuntimeException e) { append("状态: 失败\n错误码: 无（本地异常）\n错误描述: " + e); }
    }

    public void connectionAction(boolean connect) {
        if (closed || api != ApiCatalog.Api.LISTEN_CONNECTION) return;
        reset(connect ? "连接" : "断开连接");
        try {
            if (execution == null) {
                execution = factory.create(new ApiRequest(api, Collections.emptyMap(), null, System.currentTimeMillis()), scopedLog());
                append("尚未设置本页监听；点击设置连接状态监听可观察状态变化。");
            }
            if (connect) execution.connectForDemo(identity.token());
            else execution.disconnectForDemo();
        } catch (RuntimeException e) {
            append("状态: 调用异常\n错误码: 无（本地异常）\n异常类型: " + e.getClass().getSimpleName());
        }
    }

    public void stopListening() {
        if (closed) return;
        release();
        append("状态: 监听已停止");
    }
    public void onHidden() {
        if (!closed && api.isListener()) {
            release();
            append("状态: 页面已离开，本页监听已停止；返回后请重新设置监听");
        }
    }

    private ApiLog scopedLog() {
        final long owner = generation;
        return new ApiLog() {
            private final BoundedLog pending = new BoundedLog();
            private boolean scheduled;
            private long pendingRevision;
            @Override public boolean isActive() { return !closed && owner == generation; }
            @Override public synchronized void write(String entry) {
                if (!isActive()) return;
                long revision = logRevision;
                if (pendingRevision != revision) {
                    pending.clear();
                    pendingRevision = revision;
                }
                pending.append("\n" + entry + "\n");
                if (scheduled) return;
                scheduled = true;
                ui.execute(this::drain);
            }
            private void drain() {
                String batch;
                long revision;
                synchronized (this) {
                    batch = pending.toString();
                    revision = pendingRevision;
                    pending.clear();
                    scheduled = false;
                }
                // Both generation and operation revision must still match after UI dispatch.
                if (isActive() && revision == logRevision) append(batch);
            }
        };
    }

    private void reset(String action) {
        logRevision++;
        text.clear();
        header = "功能: " + api.title + "\n接口: " + api.method + "\n操作: " + action + "\n时间: "
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(new Date()) + "\n";
        render();
    }
    private void append(String entry) { text.append("\n" + entry + "\n"); render(); }
    private void render() { if (view != null && !closed) view.showLog(header + text.toString()); }
    private void release() {
        generation++;
        ApiExecution previous = execution;
        execution = null;
        if (previous != null) {
            try { previous.close(); }
            catch (RuntimeException e) { append("监听资源释放异常: " + e.getClass().getSimpleName()); }
        }
    }
    @Override public void close() {
        if (closed) return;
        closed = true;
        view = null;
        release();
        text.clear();
    }
}
