package cn.rongcloud.um.base;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import cn.rongcloud.um.auth.LoginSession;
import io.rong.imkit.RongIM;
import io.rong.imkit.IMCenter;

import cn.rongcloud.um.utils.SessionManager;

public class MyApplication extends Application {
    private static LoginSession loginSession;

    public static LoginSession loginSession() { return loginSession; }

    public static void signOut() {
        loginSession.clear();
        if (IMCenter.getInstance().isInitialized()) RongIM.getInstance().logout();
    }
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        SessionManager.initContext(this);
        SessionManager preferences = SessionManager.getInstance();
        preferences.remove("token");
        preferences.remove("appKey");
        preferences.remove("appSecret");
        loginSession = new LoginSession(new LoginSession.SelectionStore() {
            @Override
            public String read() {
                return preferences.getString(Constants.SP_KEY_LOGIN_USER);
            }

            @Override
            public void write(String userId) {
                preferences.put(Constants.SP_KEY_LOGIN_USER, userId);
            }
        });
        if (SessionManager.getInstance().getInt(Constants.SP_KEY_CONVERSATION_TYPE) == 0) {
            SessionManager.getInstance().put(Constants.SP_KEY_CONVERSATION_TYPE, 7);
            SessionManager.getInstance().put(Constants.SP_KEY_SUB_CONVERSATION_TYPE, 4);
        }
    }
}
