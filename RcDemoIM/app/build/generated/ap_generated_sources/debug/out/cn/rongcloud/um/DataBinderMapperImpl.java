package cn.rongcloud.um;

import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import androidx.databinding.DataBinderMapper;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import cn.rongcloud.um.databinding.ActivityDoMainChatRoomBindingImpl;
import cn.rongcloud.um.databinding.ActivityDoMainGroupBindingImpl;
import cn.rongcloud.um.databinding.ActivityDoMainUltraGroupBindingImpl;
import cn.rongcloud.um.databinding.ActivityDomainMessageBindingImpl;
import cn.rongcloud.um.databinding.ActivityLanguageSettingBindingImpl;
import cn.rongcloud.um.databinding.ActivityLoginBindingImpl;
import cn.rongcloud.um.databinding.ActivityMainBindingImpl;
import cn.rongcloud.um.databinding.ActivityMyMentionMemberSelectBindingImpl;
import cn.rongcloud.um.databinding.ActivityNavFileSettingBindingImpl;
import cn.rongcloud.um.databinding.ActivityParameterSettingBindingImpl;
import cn.rongcloud.um.databinding.ActivitySettingBindingImpl;
import cn.rongcloud.um.databinding.FragmentDomainListBindingImpl;
import cn.rongcloud.um.databinding.FragmentMineBindingImpl;
import cn.rongcloud.um.databinding.PopupwindowSentChatBindingImpl;
import cn.rongcloud.um.databinding.RcActivityMyConversationBindingImpl;
import cn.rongcloud.um.databinding.TitleBindingImpl;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.RuntimeException;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataBinderMapperImpl extends DataBinderMapper {
  private static final int LAYOUT_ACTIVITYDOMAINCHATROOM = 1;

  private static final int LAYOUT_ACTIVITYDOMAINGROUP = 2;

  private static final int LAYOUT_ACTIVITYDOMAINULTRAGROUP = 3;

  private static final int LAYOUT_ACTIVITYDOMAINMESSAGE = 4;

  private static final int LAYOUT_ACTIVITYLANGUAGESETTING = 5;

  private static final int LAYOUT_ACTIVITYLOGIN = 6;

  private static final int LAYOUT_ACTIVITYMAIN = 7;

  private static final int LAYOUT_ACTIVITYMYMENTIONMEMBERSELECT = 8;

  private static final int LAYOUT_ACTIVITYNAVFILESETTING = 9;

  private static final int LAYOUT_ACTIVITYPARAMETERSETTING = 10;

  private static final int LAYOUT_ACTIVITYSETTING = 11;

  private static final int LAYOUT_FRAGMENTDOMAINLIST = 12;

  private static final int LAYOUT_FRAGMENTMINE = 13;

  private static final int LAYOUT_POPUPWINDOWSENTCHAT = 14;

  private static final int LAYOUT_RCACTIVITYMYCONVERSATION = 15;

  private static final int LAYOUT_TITLE = 16;

  private static final SparseIntArray INTERNAL_LAYOUT_ID_LOOKUP = new SparseIntArray(16);

  static {
    INTERNAL_LAYOUT_ID_LOOKUP.put(cn.rongcloud.um.R.layout.activity_do_main_chat_room, LAYOUT_ACTIVITYDOMAINCHATROOM);
    INTERNAL_LAYOUT_ID_LOOKUP.put(cn.rongcloud.um.R.layout.activity_do_main_group, LAYOUT_ACTIVITYDOMAINGROUP);
    INTERNAL_LAYOUT_ID_LOOKUP.put(cn.rongcloud.um.R.layout.activity_do_main_ultra_group, LAYOUT_ACTIVITYDOMAINULTRAGROUP);
    INTERNAL_LAYOUT_ID_LOOKUP.put(cn.rongcloud.um.R.layout.activity_domain_message, LAYOUT_ACTIVITYDOMAINMESSAGE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(cn.rongcloud.um.R.layout.activity_language_setting, LAYOUT_ACTIVITYLANGUAGESETTING);
    INTERNAL_LAYOUT_ID_LOOKUP.put(cn.rongcloud.um.R.layout.activity_login, LAYOUT_ACTIVITYLOGIN);
    INTERNAL_LAYOUT_ID_LOOKUP.put(cn.rongcloud.um.R.layout.activity_main, LAYOUT_ACTIVITYMAIN);
    INTERNAL_LAYOUT_ID_LOOKUP.put(cn.rongcloud.um.R.layout.activity_my_mention_member_select, LAYOUT_ACTIVITYMYMENTIONMEMBERSELECT);
    INTERNAL_LAYOUT_ID_LOOKUP.put(cn.rongcloud.um.R.layout.activity_nav_file_setting, LAYOUT_ACTIVITYNAVFILESETTING);
    INTERNAL_LAYOUT_ID_LOOKUP.put(cn.rongcloud.um.R.layout.activity_parameter_setting, LAYOUT_ACTIVITYPARAMETERSETTING);
    INTERNAL_LAYOUT_ID_LOOKUP.put(cn.rongcloud.um.R.layout.activity_setting, LAYOUT_ACTIVITYSETTING);
    INTERNAL_LAYOUT_ID_LOOKUP.put(cn.rongcloud.um.R.layout.fragment_domain_list, LAYOUT_FRAGMENTDOMAINLIST);
    INTERNAL_LAYOUT_ID_LOOKUP.put(cn.rongcloud.um.R.layout.fragment_mine, LAYOUT_FRAGMENTMINE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(cn.rongcloud.um.R.layout.popupwindow_sent_chat, LAYOUT_POPUPWINDOWSENTCHAT);
    INTERNAL_LAYOUT_ID_LOOKUP.put(cn.rongcloud.um.R.layout.rc_activity_my_conversation, LAYOUT_RCACTIVITYMYCONVERSATION);
    INTERNAL_LAYOUT_ID_LOOKUP.put(cn.rongcloud.um.R.layout.title, LAYOUT_TITLE);
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View view, int layoutId) {
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = view.getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
        case  LAYOUT_ACTIVITYDOMAINCHATROOM: {
          if ("layout/activity_do_main_chat_room_0".equals(tag)) {
            return new ActivityDoMainChatRoomBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_do_main_chat_room is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYDOMAINGROUP: {
          if ("layout/activity_do_main_group_0".equals(tag)) {
            return new ActivityDoMainGroupBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_do_main_group is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYDOMAINULTRAGROUP: {
          if ("layout/activity_do_main_ultra_group_0".equals(tag)) {
            return new ActivityDoMainUltraGroupBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_do_main_ultra_group is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYDOMAINMESSAGE: {
          if ("layout/activity_domain_message_0".equals(tag)) {
            return new ActivityDomainMessageBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_domain_message is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYLANGUAGESETTING: {
          if ("layout/activity_language_setting_0".equals(tag)) {
            return new ActivityLanguageSettingBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_language_setting is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYLOGIN: {
          if ("layout/activity_login_0".equals(tag)) {
            return new ActivityLoginBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_login is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYMAIN: {
          if ("layout/activity_main_0".equals(tag)) {
            return new ActivityMainBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_main is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYMYMENTIONMEMBERSELECT: {
          if ("layout/activity_my_mention_member_select_0".equals(tag)) {
            return new ActivityMyMentionMemberSelectBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_my_mention_member_select is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYNAVFILESETTING: {
          if ("layout/activity_nav_file_setting_0".equals(tag)) {
            return new ActivityNavFileSettingBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_nav_file_setting is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYPARAMETERSETTING: {
          if ("layout/activity_parameter_setting_0".equals(tag)) {
            return new ActivityParameterSettingBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_parameter_setting is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYSETTING: {
          if ("layout/activity_setting_0".equals(tag)) {
            return new ActivitySettingBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_setting is invalid. Received: " + tag);
        }
        case  LAYOUT_FRAGMENTDOMAINLIST: {
          if ("layout/fragment_domain_list_0".equals(tag)) {
            return new FragmentDomainListBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for fragment_domain_list is invalid. Received: " + tag);
        }
        case  LAYOUT_FRAGMENTMINE: {
          if ("layout/fragment_mine_0".equals(tag)) {
            return new FragmentMineBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for fragment_mine is invalid. Received: " + tag);
        }
        case  LAYOUT_POPUPWINDOWSENTCHAT: {
          if ("layout/popupwindow_sent_chat_0".equals(tag)) {
            return new PopupwindowSentChatBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for popupwindow_sent_chat is invalid. Received: " + tag);
        }
        case  LAYOUT_RCACTIVITYMYCONVERSATION: {
          if ("layout/rc_activity_my_conversation_0".equals(tag)) {
            return new RcActivityMyConversationBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for rc_activity_my_conversation is invalid. Received: " + tag);
        }
        case  LAYOUT_TITLE: {
          if ("layout/title_0".equals(tag)) {
            return new TitleBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for title is invalid. Received: " + tag);
        }
      }
    }
    return null;
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View[] views, int layoutId) {
    if(views == null || views.length == 0) {
      return null;
    }
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = views[0].getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
      }
    }
    return null;
  }

  @Override
  public int getLayoutId(String tag) {
    if (tag == null) {
      return 0;
    }
    Integer tmpVal = InnerLayoutIdLookup.sKeys.get(tag);
    return tmpVal == null ? 0 : tmpVal;
  }

  @Override
  public String convertBrIdToString(int localId) {
    String tmpVal = InnerBrLookup.sKeys.get(localId);
    return tmpVal;
  }

  @Override
  public List<DataBinderMapper> collectDependencies() {
    ArrayList<DataBinderMapper> result = new ArrayList<DataBinderMapper>(1);
    result.add(new androidx.databinding.library.baseAdapters.DataBinderMapperImpl());
    return result;
  }

  private static class InnerBrLookup {
    static final SparseArray<String> sKeys = new SparseArray<String>(2);

    static {
      sKeys.put(0, "_all");
      sKeys.put(1, "listener");
    }
  }

  private static class InnerLayoutIdLookup {
    static final HashMap<String, Integer> sKeys = new HashMap<String, Integer>(16);

    static {
      sKeys.put("layout/activity_do_main_chat_room_0", cn.rongcloud.um.R.layout.activity_do_main_chat_room);
      sKeys.put("layout/activity_do_main_group_0", cn.rongcloud.um.R.layout.activity_do_main_group);
      sKeys.put("layout/activity_do_main_ultra_group_0", cn.rongcloud.um.R.layout.activity_do_main_ultra_group);
      sKeys.put("layout/activity_domain_message_0", cn.rongcloud.um.R.layout.activity_domain_message);
      sKeys.put("layout/activity_language_setting_0", cn.rongcloud.um.R.layout.activity_language_setting);
      sKeys.put("layout/activity_login_0", cn.rongcloud.um.R.layout.activity_login);
      sKeys.put("layout/activity_main_0", cn.rongcloud.um.R.layout.activity_main);
      sKeys.put("layout/activity_my_mention_member_select_0", cn.rongcloud.um.R.layout.activity_my_mention_member_select);
      sKeys.put("layout/activity_nav_file_setting_0", cn.rongcloud.um.R.layout.activity_nav_file_setting);
      sKeys.put("layout/activity_parameter_setting_0", cn.rongcloud.um.R.layout.activity_parameter_setting);
      sKeys.put("layout/activity_setting_0", cn.rongcloud.um.R.layout.activity_setting);
      sKeys.put("layout/fragment_domain_list_0", cn.rongcloud.um.R.layout.fragment_domain_list);
      sKeys.put("layout/fragment_mine_0", cn.rongcloud.um.R.layout.fragment_mine);
      sKeys.put("layout/popupwindow_sent_chat_0", cn.rongcloud.um.R.layout.popupwindow_sent_chat);
      sKeys.put("layout/rc_activity_my_conversation_0", cn.rongcloud.um.R.layout.rc_activity_my_conversation);
      sKeys.put("layout/title_0", cn.rongcloud.um.R.layout.title);
    }
  }
}
