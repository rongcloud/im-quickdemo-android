package cn.rongcloud.um.ui;

import android.text.TextUtils;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import cn.rongcloud.um.R;
import cn.rongcloud.um.base.BaseActivity;
import cn.rongcloud.um.bean.ChannelInfo;
import cn.rongcloud.um.databinding.ActivityDoMainUltraGroupBinding;
import cn.rongcloud.um.net.HttpQUtils;

import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imlib.ChannelClient;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

public class DoMainUltraGroupActivity extends BaseActivity<MainViewModel, ActivityDoMainUltraGroupBinding> {
    private static final String TAG = DoMainUltraGroupActivity.class.getSimpleName();

    @Override
    public int getContentViewId() {
        return R.layout.activity_do_main_ultra_group;
    }

    @Override
    public void setView() {
        mViewData.contentListview.setAdapter(new ArrayAdapter<>(this,
                R.layout.picklist_text,
                new String[]{"创建超级群(server API)", "创建频道(server API)", "加入超级群(server API)"
                        , "获取本地频道列表", "发送超级群消息", "多端同步消息阅读状态(清除未读数)"}));
        initListener();
    }

    @Override
    public void setData() {

    }

    private void initListener() {
        mViewData.contentListview.setOnItemClickListener((adapterView, view, i, l) -> {
            if (TextUtils.isEmpty(mViewData.inputTextUltraGroupId.getEtContentText())) {
                toast("请输入超级群id");
                return;
            }
            switch (i) {
                case 0:
                    HttpQUtils.createUltraGroup(RongIM.getInstance().getCurrentUserId(), mViewData.inputTextUltraGroupId.getEtContentText(), mViewData.inputTextUltraGroupId.getEtContentText(), new HttpQUtils.CreateUltraGroupCallBack() {
                        @Override
                        public void onSuccess() {
                            toast("创建成功");
                        }

                        @Override
                        public void onFailed(@NonNull String err) {
                            toast("code:  " + err);
                        }
                    });
                    break;
                case 1:
                    if (TextUtils.isEmpty(mViewData.inputTextChannelId.getEtContentText())) {
                        toast("请输入频道id");
                        return;
                    }
                    HttpQUtils.createChannel(mViewData.inputTextUltraGroupId.getEtContentText(), mViewData.inputTextChannelId.getEtContentText(), new HttpQUtils.CreateChannelCallBack() {
                        @Override
                        public void onSuccess() {
                            toast("创建成功");
                        }

                        @Override
                        public void onFailed(@NonNull String err) {
                            toast("code:  " + err);
                        }
                    });
                    break;
                case 2:
                    HttpQUtils.joinUltraGroup(RongIM.getInstance().getCurrentUserId(), mViewData.inputTextUltraGroupId.getEtContentText(), new HttpQUtils.JoinUltraGroupCallBack() {
                        @Override
                        public void onSuccess() {
                            toast("加入成功");
                        }

                        @Override
                        public void onFailed(@NonNull String err) {
                            toast("code:  " + err);
                        }
                    });
                    break;
                case 3:
                    HttpQUtils.getChannelList(mViewData.inputTextUltraGroupId.getEtContentText(), 1, 10, new HttpQUtils.GetChannelInfosCallBack() {
                        @Override
                        public void onSuccess(List<ChannelInfo> channelInfos) {
                            toast("获取成功");
                        }

                        @Override
                        public void onFailed(@NonNull String err) {
                            toast("code:  " + err);
                        }
                    });
                    break;
                case 4:
                    if (TextUtils.isEmpty(mViewData.inputTextChannelId.getEtContentText())) {
                        toast("请输入频道id");
                        return;
                    }
                    TextMessage textMessage = TextMessage.obtain("测试超级群");
                    ChannelClient.getInstance().sendMessage(Conversation.ConversationType.ULTRA_GROUP, mViewData.inputTextUltraGroupId.getEtContentText(), mViewData.inputTextChannelId.getEtContentText(), textMessage,
                            "", "", new IRongCoreCallback.ISendMediaMessageCallback() {
                                @Override
                                public void onProgress(Message message, int progress) {

                                }

                                @Override
                                public void onCanceled(Message message) {

                                }

                                @Override
                                public void onAttached(Message message) {

                                }

                                @Override
                                public void onSuccess(Message message) {
                                    toast("发送成功");
                                }

                                @Override
                                public void onError(Message message, IRongCoreEnum.CoreErrorCode coreErrorCode) {
                                    toast("code:  " + coreErrorCode);
                                }
                            });
                    break;
                case 5:
                    if (TextUtils.isEmpty(mViewData.inputTextChannelId.getEtContentText())) {
                        toast("请输入频道id");
                        return;
                    }
                    ChannelClient.getInstance().syncUltraGroupReadStatus(mViewData.inputTextUltraGroupId.getEtContentText(), mViewData.inputTextChannelId.getEtContentText(),
                            0, new IRongCoreCallback.OperationCallback() {
                                @Override
                                public void onSuccess() {
                                    toast("同步已读状态成功");
                                }

                                @Override
                                public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                                    toast("code:  " + coreErrorCode);
                                }
                            });
                    break;
            }
        });
    }
}