package cn.rongcloud.um.sdk;

import android.app.Application;

import cn.rongcloud.um.base.Constants;
import cn.rongcloud.um.utils.SessionManager;

import io.rong.common.rlog.RLog;
import io.rong.imkit.IMCenter;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.InitOption;

/** One-time process initialization, independent of Activity recreation and account switching. */
public final class SdkInitializer {
    private static String initializedKey;
    private SdkInitializer() { }

    public static synchronized void initialize(Application application, String appKey) {
        if (initializedKey != null) {
            if (!initializedKey.equals(appKey)) throw new IllegalStateException("修改 AppKey 后请重启应用");
            return;
        }
        SessionManager settings = SessionManager.getInstance();
        String navigation = settings.getString(Constants.SP_KEY_NAV_URL);
        if (!navigation.isEmpty()) {
            RongIMClient.setServerInfo(navigation, settings.getString(Constants.SP_KEY_FILE_URL));
        }

        InitOption initOption = new InitOption.Builder().build();
        IMCenter.init(application, appKey, initOption);

        // 初始化之后打开融云日志打印
        RongCoreClient.getInstance().setRLogLevel(RLog.V);

        ImKitDemoSetup.install();
        initializedKey = appKey;

    }

}
