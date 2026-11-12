package cn.rongcloud.um.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String SETTIING_OPTIONS = "SETTIING_OPTIONS";
    private static SharedPreferences sharedPreferences;
    private static SessionManager sessionManager;
    private static Context mContext;

    public static void initContext(Context context) {
        mContext = context;
    }

    public static SessionManager getInstance() {
        synchronized (SessionManager.class) {
            if (sessionManager == null && mContext != null) {
                sessionManager = new SessionManager(mContext);
            }
        }
        return sessionManager;
    }

    private SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(SETTIING_OPTIONS, Context.MODE_PRIVATE);
    }

    public String put(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
        return value;
    }

    public int put(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
        return value;
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param key
     */
    public void remove(String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, "");
    }

    public int getInt(String key) {
        return sharedPreferences.getInt(key, 0);
    }

    public int getInt(String key, int defValue) {
        return sharedPreferences.getInt(key, defValue);
    }

}
