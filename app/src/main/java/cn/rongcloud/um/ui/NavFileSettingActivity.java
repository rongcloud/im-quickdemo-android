package cn.rongcloud.um.ui;

import android.text.TextUtils;

import cn.rongcloud.um.R;
import cn.rongcloud.um.base.BaseActivity;
import cn.rongcloud.um.base.Constants;
import cn.rongcloud.um.databinding.ActivityNavFileSettingBinding;
import cn.rongcloud.um.utils.SessionManager;

public class NavFileSettingActivity extends BaseActivity<MainViewModel, ActivityNavFileSettingBinding> {

    @Override
    public int getContentViewId() {
        return R.layout.activity_nav_file_setting;
    }

    @Override
    public void setView() {
        initListener();
    }

    private void initListener() {
        mViewData.topBar.setOnRightClickListener(view -> {
            if (!TextUtils.isEmpty(mViewData.inputTextNav.getEtContentText())) {
                SessionManager.getInstance().put(Constants.SP_KEY_NAV_URL, mViewData.inputTextNav.getEtContentText());
            }
            if (!TextUtils.isEmpty(mViewData.inputTextFile.getEtContentText())) {
                SessionManager.getInstance().put(Constants.SP_KEY_FILE_URL, mViewData.inputTextFile.getEtContentText());
            }
            toast("点击保存");
        });
    }

    @Override
    public void setData() {
        if (!TextUtils.isEmpty(SessionManager.getInstance().getString(Constants.SP_KEY_NAV_URL))) {
            mViewData.inputTextNav.getEtContent().setText(SessionManager.getInstance().getString(Constants.SP_KEY_NAV_URL));
        }
        if (!TextUtils.isEmpty(SessionManager.getInstance().getString(Constants.SP_KEY_FILE_URL))) {
            mViewData.inputTextFile.getEtContent().setText(SessionManager.getInstance().getString(Constants.SP_KEY_FILE_URL));
        }
    }
}