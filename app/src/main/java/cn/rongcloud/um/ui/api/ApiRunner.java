package cn.rongcloud.um.ui.api;

/** Validates input, resolves one registered action, and owns its request scope. */
public final class ApiRunner implements ApiExecution {
    private final ApiRequest request;
    private final ApiLog log;
    private final ApiActionRegistry registry;
    private ApiContext context;
    private boolean closed;

    public ApiRunner(ApiRequest request, ApiLog log) {
        this(request, log, ApiActionRegistry.defaults());
    }
    ApiRunner(ApiRequest request, ApiLog log, ApiActionRegistry registry) {
        this.request = request;
        this.log = log;
        this.registry = registry;
    }
    private ApiContext context() {
        if (closed) throw new IllegalStateException("请求已关闭");
        if (context == null) context = new ApiContext(request, log);
        return context;
    }
    @Override public void execute() {
        if (closed || !log.isActive()) return;
        try {
            request.validate();
            log.write("实际 SDK 参数:\n" + ApiLogFormatter.format(request.sdkValues()));
            registry.action(request.api).execute(context());
        } catch (RuntimeException e) {
            log.write("状态: 失败\n错误码: 无（本地参数校验或调用异常）\n错误描述: " + e);
        }
    }
    private ConnectionDemo connection() {
        if (request.api != ApiCatalog.Api.LISTEN_CONNECTION) throw new IllegalStateException("仅连接监听页支持连接操作");
        return context().connection();
    }
    @Override public void connectForDemo(String token) { if (!closed && log.isActive()) connection().connect(token); }
    @Override public void disconnectForDemo() { if (!closed && log.isActive()) connection().disconnect(); }
    @Override public void close() {
        if (closed) return;
        closed = true;
        if (context != null) context.close();
    }
}
