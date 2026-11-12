package cn.rongcloud.um.ui.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import cn.rongcloud.um.R;
import cn.rongcloud.um.base.BaseFragment;
import cn.rongcloud.um.base.MyApplication;
import cn.rongcloud.um.base.Constants;
import cn.rongcloud.um.utils.SessionManager;
import cn.rongcloud.um.databinding.FragmentMineBinding;
import cn.rongcloud.um.ui.LanguageSettingActivity;
import cn.rongcloud.um.ui.LoginActivity;
import io.rong.imkit.RongIM;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.userinfo.model.GroupUserInfo;
import io.rong.imlib.ChannelClient;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.UserInfo;

public class MineFragment extends BaseFragment<FragmentMineBinding> implements View.OnClickListener, RongUserInfoManager.UserDataObserver {
    private boolean isBlock;
    private boolean isPushContent;

    @Override
    public int getContentViewId() {
        return R.layout.fragment_mine;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        mViewData.rcSwitchNotDisturb.setEnabled(false);
        mViewData.rcSwitchRemotePushContent.setEnabled(false);
        getNotificationQuietHoursLevel();
        initListener();
        getUserPortrait();
        getPushContentShowStatus();
        RongUserInfoManager.getInstance().addUserDataObserver(this);
    }

    private void getNotificationQuietHoursLevel() {
        final FragmentMineBinding binding = mViewData;
        ChannelClient.getInstance().getNotificationQuietHoursLevel(new IRongCoreCallback.GetNotificationQuietHoursCallbackEx() {
            @Override
            public void onSuccess(String startTime, int spanMinutes, IRongCoreEnum.PushNotificationQuietHoursLevel level) {
                withView(binding, () -> {
                    binding.rcSwitchNotDisturb.setEnabled(true);
                    if (TextUtils.isEmpty(startTime)) {
                        isBlock = false;
                        binding.rcSwitchNotDisturb.setChecked(false);
                    } else {
                        isBlock = true;
                        binding.rcSwitchNotDisturb.setChecked(true);
                    }
                });
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                withView(binding, () -> {
                    binding.rcSwitchNotDisturb.setEnabled(true);
                    toast("获取免打扰状态失败：" + coreErrorCode.getValue());
                });
            }
        });
    }

    private void getPushContentShowStatus() {
        final FragmentMineBinding binding = mViewData;
        RongIMClient.getInstance().getPushContentShowStatus(new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                withView(binding, () -> {
                    binding.rcSwitchRemotePushContent.setEnabled(true);
                    isPushContent = Boolean.TRUE.equals(aBoolean);
                    binding.rcSwitchRemotePushContent.setChecked(isPushContent);
                });
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                withView(binding, () -> {
                    binding.rcSwitchRemotePushContent.setEnabled(true);
                    toast("获取推送状态失败：" + e.getValue());
                });
            }
        });
    }

    private void getUserPortrait() {
        if (mViewData == null) return;
        String currentUserId = RongIM.getInstance().getCurrentUserId();
        if (currentUserId == null) return;
        mViewData.rcUser.setText(currentUserId);
        mViewData.rcUserId.setText(getString(R.string.mine_user_id, currentUserId));
        Glide.with(mViewData.rcMinePortrait).clear(mViewData.rcMinePortrait);
        mViewData.rcMinePortrait.setImageResource(R.drawable.ic_mine_avatar);
        UserInfo userInfo = RongUserInfoManager.getInstance()
        .getUserInfo(currentUserId);
        if (userInfo != null) {
            if (!TextUtils.isEmpty(userInfo.getName())) mViewData.rcUser.setText(userInfo.getName());
            Uri uri = userInfo.getPortraitUri();
            if (uri != null) {
                Glide.with(mViewData.rcMinePortrait).load(uri).placeholder(R.drawable.ic_mine_avatar).error(R.drawable.ic_mine_avatar).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(mViewData.rcMinePortrait);
            }
        }
    }

    private void initListener() {
        mViewData.rcLayout.setOnClickListener(this);
        mViewData.languageRow.setOnClickListener(this);
        mViewData.rcSwitchNotDisturb.setOnClickListener(this);
        mViewData.rcSwitchRemotePushContent.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        final FragmentMineBinding binding = mViewData;
        if (view.getId() == R.id.rc_layout) {
            MyApplication.signOut();
            requireActivity().finish();
            goActivity(LoginActivity.class);
        } else if (view.getId() == R.id.rc_switch_not_disturb) {
            binding.rcSwitchNotDisturb.setEnabled(false);
            if (isBlock) {
                ChannelClient.getInstance().removeNotificationQuietHours(new IRongCoreCallback.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        withView(binding, () -> {
                            binding.rcSwitchNotDisturb.setEnabled(true);
                            isBlock = false;
                            binding.rcSwitchNotDisturb.setChecked(false);
                        });
                    }

                    @Override
                    public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                        withView(binding, () -> {
                            binding.rcSwitchNotDisturb.setEnabled(true);
                            toast("移除失败，coreErrorCode： " + coreErrorCode.getValue());
                            binding.rcSwitchNotDisturb.setChecked(true);
                        });
                    }
                });
            } else {
                setNotificationQuietHoursLevel();
            }
        } else if (view.getId() == R.id.language_row) {
            goActivity(LanguageSettingActivity.class);
        } else if (view.getId() == R.id.rc_switch_remote_push_content) {
            final boolean desired = binding.rcSwitchRemotePushContent.isChecked();
            binding.rcSwitchRemotePushContent.setEnabled(false);
            RongIMClient.getInstance().setPushContentShowStatus(desired, new RongIMClient.OperationCallback() {
                @Override
                public void onSuccess() {
                    withView(binding, () -> {
                        binding.rcSwitchRemotePushContent.setEnabled(true);
                        isPushContent = desired;
                        binding.rcSwitchRemotePushContent.setChecked(isPushContent);
                    });
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {
                    withView(binding, () -> {
                        binding.rcSwitchRemotePushContent.setEnabled(true);
                        binding.rcSwitchRemotePushContent.setChecked(isPushContent);
                        toast("修改失败，errorCode： " + errorCode.getValue());
                    });
                }
            });
        }
    }

    private void setNotificationQuietHoursLevel() {
        final FragmentMineBinding binding = mViewData;
        String startTime = "00:00:00";
        int spanMinutes = 1439;

        ChannelClient.getInstance().setNotificationQuietHoursLevel(startTime, spanMinutes,
        IRongCoreEnum.PushNotificationQuietHoursLevel.PUSH_NOTIFICATION_QUIET_HOURS_LEVEL_BLOCKED,
        new IRongCoreCallback.OperationCallback() {
            @Override
            public void onSuccess() {
                withView(binding, () -> {
                    binding.rcSwitchNotDisturb.setEnabled(true);
                    isBlock = true;
                    binding.rcSwitchNotDisturb.setChecked(true);
                });
            }

            @Override
            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                withView(binding, () -> {
                    binding.rcSwitchNotDisturb.setEnabled(true);
                    toast("设置失败，coreErrorCode： " + coreErrorCode.getValue());
                    binding.rcSwitchNotDisturb.setChecked(false);
                });
            }
        });
    }

    @Override public void onResume() {
        super.onResume();
        if (mViewData == null) return;
        String[] languages = getResources().getStringArray(R.array.mine_languages);
        int selected = SessionManager.getInstance().getInt(Constants.SP_KEY_LANGUAGE_SETTING, 0);
        mViewData.tvLanguageChoose.setText(languages[selected >= 0 && selected < languages.length ? selected : 0]);
    }

    @Override
    public void onDestroyView() {
        RongUserInfoManager.getInstance().removeUserDataObserver(this);
        super.onDestroyView();
    }

    @Override
    public void onUserUpdate(UserInfo info) {
        withView(mViewData, this::getUserPortrait);
    }

    @Override
    public void onGroupUpdate(Group group) {

    }

    @Override
    public void onGroupUserInfoUpdate(GroupUserInfo groupUserInfo) {

    }
}
