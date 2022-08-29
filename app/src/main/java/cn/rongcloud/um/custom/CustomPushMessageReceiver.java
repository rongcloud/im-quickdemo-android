package cn.rongcloud.um.custom;

import android.content.Context;

import com.huawei.hms.support.api.entity.core.CommonCode;

import io.rong.push.PushType;
import io.rong.push.notification.PushMessageReceiver;
import io.rong.push.notification.PushNotificationMessage;

public class CustomPushMessageReceiver extends PushMessageReceiver {
    public static boolean needUpdate = false;
    @Override
    public boolean onNotificationMessageClicked(Context context, PushType pushType, PushNotificationMessage notificationMessage) {

        return super.onNotificationMessageClicked(context, pushType, notificationMessage);
    }
    //华为获取 token 异常回调此方法
    @Override
    public void onThirdPartyPushState(PushType pushType, String action, long resultCode) {
        super.onThirdPartyPushState(pushType, action, resultCode);
        if (pushType.equals(PushType.HUAWEI)) {
            if (resultCode == CommonCode.ErrorCode.CLIENT_API_INVALID) {
                //设置标记位，引导用户升级
                needUpdate = true;
            }
        }
    }
}
