package cn.rongcloud.um.auth;

import android.app.Application;
import cn.rongcloud.um.sdk.SdkInitializer;
import io.rong.imkit.IMCenter;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.RongIMClient;

/** Android/SDK adapter; no Activity reference is retained. */
public final class RongLoginClient implements LoginController.Client {
    private final Application application;
    public RongLoginClient(Application application) { this.application = application; }
    @Override public void initialize(String appKey) { SdkInitializer.initialize(application, appKey); }
    @Override public void disconnect() {
        if (IMCenter.getInstance().isInitialized()) RongIM.getInstance().disconnect();
    }
    @Override public void connect(String token, LoginController.Callback callback) {
        IMCenter.getInstance().connect(token, new RongIMClient.ConnectCallback() {
            @Override public void onSuccess(String userId) { callback.success(userId); }
            @Override public void onError(RongIMClient.ConnectionErrorCode error) {
                if (error == RongIMClient.ConnectionErrorCode.RC_CONNECTION_EXIST) {
                    callback.success(RongCoreClient.getInstance().getCurrentUserId());
                } else callback.failure(error.getValue(), error.name());
            }
            @Override public void onDatabaseOpened(RongIMClient.DatabaseOpenStatus status) { }
        });
    }
}
