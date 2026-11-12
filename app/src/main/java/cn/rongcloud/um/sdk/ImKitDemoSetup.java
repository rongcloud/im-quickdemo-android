package cn.rongcloud.um.sdk;

import cn.rongcloud.um.ui.MainActivity;
import cn.rongcloud.um.ui.MyConversationActivity;
import java.util.ArrayList;

import cn.rongcloud.um.custom.CustomMessage;
import cn.rongcloud.um.custom.CustomMessageProvider;
import cn.rongcloud.um.custom.MyDataProcessor;
import cn.rongcloud.um.custom.MyExtensionConfig;
import cn.rongcloud.um.utils.DataProcessorUtil;

import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.conversation.extension.RongExtensionManager;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.userinfo.model.GroupUserInfo;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;

/** Registration for the scenarios exposed in the IMKit tab. Called once by SdkInitializer. */
public final class ImKitDemoSetup {
    private ImKitDemoSetup() { }
    static void install() {
        // 在打开会话列表页面之前，调用设置自定义的数据处理器
        DataProcessorUtil.conversationTypes = DataProcessorUtil.supportedTypes();
        RongConfigCenter.conversationListConfig().setDataProcessor(new MyDataProcessor());
        ArrayList<Class<? extends MessageContent>> myMessages = new ArrayList<>();
        myMessages.add(CustomMessage.class);
        RongIMClient.registerMessageType(myMessages);
        RongConfigCenter.conversationConfig().addMessageProvider(new CustomMessageProvider());
        RongExtensionManager.getInstance().setExtensionConfig(new MyExtensionConfig());
        RouteUtils.registerActivity(RouteUtils.RongActivityType.ConversationListActivity, MainActivity.class);
        RouteUtils.registerActivity(RouteUtils.RongActivityType.ConversationActivity, MyConversationActivity.class);
        // 初始化用户和群组信息内容提供者
        configureProfiles();
    }

    /** Local display names only; SDK default portraits need no app-server requests. */
    private static void configureProfiles() {
        RongUserInfoManager.getInstance().setUserInfoProvider(userId ->
                new UserInfo(userId, userId, null), true);
        RongUserInfoManager.getInstance().setGroupInfoProvider(groupId ->
                new Group(groupId, groupId, null), true);
        RongUserInfoManager.getInstance().setGroupUserInfoProvider((groupId, userId) ->
                new GroupUserInfo(groupId, userId, userId), true);
    }
}
