package cn.rongcloud.um.ui;

import android.content.Intent;
import android.os.Looper;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.um.R;
import cn.rongcloud.um.base.BaseActivity;
import cn.rongcloud.um.databinding.ActivityMainBinding;
import cn.rongcloud.um.ui.adapter.ViewPagerAdapter;
import cn.rongcloud.um.ui.fragment.DoMainListFragment;
import cn.rongcloud.um.ui.fragment.MineFragment;
import cn.rongcloud.um.ui.fragment.ImKitListFragment;
import io.rong.imkit.conversationlist.ConversationListFragment;
import cn.rongcloud.um.ui.widget.BottomTabLayout;
import cn.rongcloud.um.utils.DataProcessorUtil;
import io.rong.imkit.IMCenter;
import io.rong.imkit.RongIM;
import io.rong.imkit.utils.ExecutorHelper;
import io.rong.imlib.IRongCoreListener;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

public class MainActivity extends BaseActivity<ActivityMainBinding> {
    public static final String EXTRA_TAB = "main_tab";
    private List<Fragment> mListFrg = new ArrayList<>();
    private long clickTime = 0; // 第一次点击的时间

    @Override protected void onCreate(Bundle state) {
        // Embedded IMKit fragments require the SDK color/theme attributes.
        getTheme().applyStyle(io.rong.imkit.R.style.RCTraditionLightTheme, true);
        super.onCreate(state);
        if (state == null && mViewData != null) {
            int tab = getIntent().getIntExtra(EXTRA_TAB, 0);
            if (tab >= 0 && tab < mListFrg.size()) mViewData.container.setCurrentItem(tab, false);
        }
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    public void setView() {
        initBottomTab();
        initListener();
    }

    private void initListener() {
        RongIM.addOnReceiveMessageListener(onReceiveMessageWrapperListener);
        IMCenter.getInstance().addSyncConversationReadStatusListener(syncConversationReadStatusListener);
        RongCoreClient.addConnectionStatusListener(conversationStatusListener);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mViewData == null) return;
        getUnreadCount();
        String[] titles = {"会话列表", "IMLibCore", "IMKit", "我的"};
        int page = mViewData.container.getCurrentItem();
        mViewData.topBar.setCenterText(titles[page]);
    }

    private void initBottomTab() {
        mListFrg.add(new ConversationListFragment());
        mListFrg.add(new DoMainListFragment());
        mListFrg.add(new ImKitListFragment());
        mListFrg.add(new MineFragment());
        ViewPagerAdapter mainFragmentAdapter = new ViewPagerAdapter(getSupportFragmentManager(), mListFrg);
        mViewData.container.setAdapter(mainFragmentAdapter);
        mViewData.container.setOffscreenPageLimit(1);
        mViewData.mainBottomBarr.setUpWithViewPager(mViewData.container);
        mViewData.mainBottomBarr.setOnItemTabClickListener(new BottomTabLayout.OnItemTabClickListener() {
            @Override
            public boolean onItemTabClick(int position, View itemView) {
                switch (position) {
                    case 0:
                        mViewData.topBar.setCenterText("会话列表");
                        break;
                    case 1:
                        mViewData.topBar.setCenterText("IMLibCore");
                        break;
                    case 2:
                        mViewData.topBar.setCenterText("IMKit");
                        break;
                    case 3:
                        mViewData.topBar.setCenterText("我的");
                        break;


                }
                return false;
            }
        });
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        int tab = intent.getIntExtra(EXTRA_TAB, -1);
        if (tab >= 0 && tab < mListFrg.size()) mViewData.container.setCurrentItem(tab, false);
    }

    @Override
    protected void onDestroy() {
        RongIM.removeOnReceiveMessageListener(onReceiveMessageWrapperListener);
        IMCenter.getInstance().removeSyncConversationReadStatusListeners(syncConversationReadStatusListener);
        RongCoreClient.removeConnectionStatusListener(conversationStatusListener);
        super.onDestroy();
    }

    RongIMClient.OnReceiveMessageWrapperListener onReceiveMessageWrapperListener = new RongIMClient.OnReceiveMessageWrapperListener() {
        @Override
        public boolean onReceived(Message message, int left, boolean hasPackage, boolean offline) {
            Log.i("message",message.toString());
            getUnreadCount();
            return false;
        }
    };
    IRongCoreListener.ConnectionStatusListener conversationStatusListener = new IRongCoreListener.ConnectionStatusListener() {
        @Override
        public void onChanged(ConnectionStatus status) {
            Log.e("ConversationStatus", "status: " + status);
        }
    };

    private void getUnreadCount() {

        RongIMClient.getInstance().getUnreadCount(DataProcessorUtil.conversationTypes, new RongIMClient.ResultCallback<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                if (Thread.currentThread().equals(Looper.getMainLooper().getThread())) {
                    mViewData.btvMessage.setCount(integer);
                } else {
                    ExecutorHelper.getInstance().mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            mViewData.btvMessage.setCount(integer);
                        }
                    });
                }

            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                toast("code: " + e);
            }
        });
    }

    RongIMClient.SyncConversationReadStatusListener syncConversationReadStatusListener = new RongIMClient.SyncConversationReadStatusListener() {
        @Override
        public void onSyncConversationReadStatus(Conversation.ConversationType type, String targetId) {
            getUnreadCount();
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 是否触发按键为back键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return true;
        } else { // 如果不是back键正常响应
            return super.onKeyDown(keyCode, event);
        }
    }

    private void exit() {
        if ((System.currentTimeMillis() - clickTime) > 2000) {
            toast("再按一次后退键退出程序");
            clickTime = System.currentTimeMillis();
        } else {
            this.finish();
            System.exit(0);
        }
    }
}
