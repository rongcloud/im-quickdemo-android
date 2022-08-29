package cn.rongcloud.um.ui;

import androidx.recyclerview.widget.LinearLayoutManager;

import cn.rongcloud.um.R;
import cn.rongcloud.um.ui.adapter.LanguageSettingAdapter;
import cn.rongcloud.um.base.BaseActivity;
import cn.rongcloud.um.base.Constants;
import cn.rongcloud.um.databinding.ActivityLanguageSettingBinding;
import cn.rongcloud.um.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class LanguageSettingActivity extends BaseActivity<MainViewModel, ActivityLanguageSettingBinding> {
    private LanguageSettingAdapter languageSettingAdapter;
    private List<String> list = new ArrayList<>();

    @Override
    public int getContentViewId() {
        return R.layout.activity_language_setting;
    }

    @Override
    public void setView() {
        mViewData.rcLanguageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        languageSettingAdapter = new LanguageSettingAdapter();
        mViewData.rcLanguageRecyclerView.setAdapter(languageSettingAdapter);
        initListener();
    }

    private void initListener() {
        mViewData.topBar.setOnRightClickListener(view -> {
            if (languageSettingAdapter == null) return;
            SessionManager.getInstance().put(Constants.SP_KEY_LANGUAGE_SETTING, languageSettingAdapter.getChooseId());
            toast("已保存");
        });
    }

    @Override
    public void setData() {
        list.add("简体中文");
        list.add("English");
        list.add("阿拉伯语");
        languageSettingAdapter.setChooseId(SessionManager.getInstance().getInt(Constants.SP_KEY_LANGUAGE_SETTING, 0));
        languageSettingAdapter.setNewList(list);
    }
}