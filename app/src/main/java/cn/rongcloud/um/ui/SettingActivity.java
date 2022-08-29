package cn.rongcloud.um.ui;

import android.view.View;

import cn.rongcloud.um.R;
import cn.rongcloud.um.base.BaseActivity;
import cn.rongcloud.um.databinding.ActivitySettingBinding;

public class SettingActivity extends BaseActivity<MainViewModel, ActivitySettingBinding> implements View.OnClickListener {
    @Override
    public int getContentViewId() {
        return R.layout.activity_setting;
    }

    @Override
    public void setView() {
        initListener();
    }

    private void initListener() {
        mViewData.tvNavigationFile.setOnClickListener(this);
        mViewData.tvConversationlistParameter.setOnClickListener(this);
    }

    @Override
    public void setData() {

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tv_navigation_file) {
            goActivity(NavFileSettingActivity.class);
        } else if (view.getId() == R.id.tv_conversationlist_parameter) {
            goActivity(ParameterSettingActivity.class);
        }
    }
}
