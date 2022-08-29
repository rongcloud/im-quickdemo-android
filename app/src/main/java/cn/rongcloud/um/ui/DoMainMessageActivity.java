package cn.rongcloud.um.ui;

import android.view.View;

import cn.rongcloud.um.R;
import cn.rongcloud.um.base.BaseActivity;
import cn.rongcloud.um.databinding.ActivityDomainMessageBinding;

import io.rong.imkit.IMCenter;
import io.rong.imkit.RongIM;

public class DoMainMessageActivity extends BaseActivity<MainViewModel, ActivityDomainMessageBinding> implements View.OnClickListener {
    @Override
    public int getContentViewId() {
        return R.layout.activity_domain_message;
    }

    @Override
    public void setView() {
        initListener();
    }

    @Override
    public void setData() {

    }

    private void initListener() {
        mViewData.tvSentVoiceMsg.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tv_sent_voice_msg) {
            RongIM.getInstance().setVoiceMessageType(IMCenter.VoiceMessageType.Ordinary);
        } else if (view.getId() == R.id.tv_sent_hq_voice_msg) {
            RongIM.getInstance().setVoiceMessageType(IMCenter.VoiceMessageType.HighQuality);
        }
    }
}
