package cn.rongcloud.um.ui.api;

import cn.rongcloud.um.base.MyApplication;

/** Shared login identity; demo disconnects keep credentials available for reconnect. */
public final class ApiIdentity implements ApiSession.Identity {
    @Override public String userId() { return MyApplication.loginSession().userId(); }
    @Override public String token() { return MyApplication.loginSession().token(); }
}
