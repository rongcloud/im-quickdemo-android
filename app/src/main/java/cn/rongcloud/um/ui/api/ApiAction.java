package cn.rongcloud.um.ui.api;

/** One executable feature, represented by a method/lambda or a composite use case. */
@FunctionalInterface
interface ApiAction {
    void execute(ApiContext context);
}
