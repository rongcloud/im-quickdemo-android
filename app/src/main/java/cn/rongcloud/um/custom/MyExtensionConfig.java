package cn.rongcloud.um.custom;

import java.util.List;

import io.rong.imkit.conversation.extension.DefaultExtensionConfig;
import io.rong.imkit.conversation.extension.component.plugin.IPluginModule;
import io.rong.imlib.model.Conversation;

public class MyExtensionConfig extends DefaultExtensionConfig {
    private static final String TAG = MyExtensionConfig.class.getSimpleName();

    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType, String targetId) {
        List<IPluginModule> pluginModules = super.getPluginModules(conversationType, targetId);

        pluginModules.add(new CustomPlugin());
        return pluginModules;
    }
}
